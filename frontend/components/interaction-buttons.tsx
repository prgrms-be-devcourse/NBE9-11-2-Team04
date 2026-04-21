"use client"

import { useCallback, useEffect, useMemo, useState } from "react"
import type React from "react"
import { Heart, Bookmark } from "lucide-react"
import { Button } from "@/components/ui/button"
import { getAccessToken, getAuthSnapshot } from "@/lib/auth-storage"

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

type InteractionButtonsProps = {
  postId: number
  initialLiked?: boolean
  initialBookmarked?: boolean
  initialLikeCount?: number
  onBookmarkToggle?: (nextBookmarked: boolean) => void
}

type LikeResponse = {
  postId: number
  liked: boolean
  likeCount: number
  message?: string
}

type BookmarkResponse = {
  postId: number
  bookmarked: boolean
  message?: string
}

type LoginRequiredPopupState = {
  open: boolean
  message: string
}

async function parseResponse(res: Response) {
  const contentType = res.headers.get("content-type") || ""
  const rawText = await res.text()

  if (contentType.includes("application/json")) {
    try {
      return JSON.parse(rawText)
    } catch {
      throw new Error("서버 JSON 응답을 읽는 중 오류가 발생했습니다.")
    }
  }

  if (rawText.includes("<!DOCTYPE html>") || rawText.includes("<html")) {
    throw new Error("서버가 JSON 대신 HTML을 반환했습니다. API 주소를 확인해주세요.")
  }

  return rawText
}

function getAuthHeaders(): HeadersInit {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  }

  const token = getAccessToken()
  if (token && token !== "oauth-cookie-session") {
    headers.Authorization = `Bearer ${token}`
  }

  return headers
}

function isUnauthorizedStatus(status: number): boolean {
  return status === 401 || status === 403
}

function isLoginRequiredMessage(message: string | null | undefined): boolean {
  if (!message) {
    return false
  }

  return message.includes("인증") || message.includes("로그인")
}

export default function InteractionButtons({
  postId,
  initialLiked = false,
  initialBookmarked = false,
  initialLikeCount = 0,
  onBookmarkToggle,
}: InteractionButtonsProps) {
  const [liked, setLiked] = useState(initialLiked)
  const [bookmarked, setBookmarked] = useState(initialBookmarked)
  const [likeCount, setLikeCount] = useState(initialLikeCount)
  const [likeLoading, setLikeLoading] = useState(false)
  const [bookmarkLoading, setBookmarkLoading] = useState(false)

  const [loginRequiredPopup, setLoginRequiredPopup] = useState<LoginRequiredPopupState>({
    open: false,
    message: "로그인이 필요한 기능입니다.",
  })

  const loginPath = useMemo(() => "/login", [])

  const openLoginRequiredPopup = useCallback((message = "로그인이 필요한 기능입니다.") => {
    setLoginRequiredPopup({
      open: true,
      message,
    })
  }, [])

  const closeLoginRequiredPopup = useCallback(() => {
    setLoginRequiredPopup((prev) => ({
      ...prev,
      open: false,
    }))
  }, [])

  const moveToLoginPage = useCallback(() => {
    if (typeof window !== "undefined") {
      window.location.href = loginPath
    }
  }, [loginPath])

  useEffect(() => {
    setLiked(initialLiked)
  }, [initialLiked])

  useEffect(() => {
    setBookmarked(initialBookmarked)
  }, [initialBookmarked])

  useEffect(() => {
    setLikeCount(initialLikeCount)
  }, [initialLikeCount])

  const requireAuth = () => {
    const auth = getAuthSnapshot()

    if (!auth.isLoggedIn) {
      openLoginRequiredPopup("로그인이 필요한 기능입니다.")
      return false
    }

    return true
  }

  const handleLike = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault()
    e.stopPropagation()

    if (!requireAuth()) return
    if (likeLoading) return

    setLikeLoading(true)

    try {
      const method = liked ? "DELETE" : "POST"

      const res = await fetch(`${API_BASE_URL}/api/posts/${postId}/likes`, {
        method,
        credentials: "include",
        headers: getAuthHeaders(),
      })

      const parsed = await parseResponse(res)

      if (!res.ok) {
        const message =
          typeof parsed === "string"
            ? parsed || "좋아요 처리 실패"
            : parsed?.message || "좋아요 처리 실패"

        if (isUnauthorizedStatus(res.status) || isLoginRequiredMessage(message)) {
          openLoginRequiredPopup("로그인이 필요한 기능입니다.")
          return
        }

        throw new Error(message)
      }

      const data = parsed as LikeResponse
      setLiked(Boolean(data.liked))
      setLikeCount(
        typeof data.likeCount === "number" ? data.likeCount : likeCount
      )

      window.dispatchEvent(new CustomEvent("notifications-updated"))
    } catch (error: any) {
      console.error("좋아요 처리 실패:", error)
      const message = error?.message || "좋아요 처리 실패"

      if (isLoginRequiredMessage(message)) {
        openLoginRequiredPopup("로그인이 필요한 기능입니다.")
        return
      }

      alert(message)
    } finally {
      setLikeLoading(false)
    }
  }

  const handleBookmark = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault()
    e.stopPropagation()

    if (!requireAuth()) return
    if (bookmarkLoading) return

    setBookmarkLoading(true)

    try {
      const method = bookmarked ? "DELETE" : "POST"

      const res = await fetch(`${API_BASE_URL}/api/posts/${postId}/bookmarks`, {
        method,
        credentials: "include",
        headers: getAuthHeaders(),
      })

      const parsed = await parseResponse(res)

      if (!res.ok) {
        const message =
          typeof parsed === "string"
            ? parsed || "북마크 처리 실패"
            : parsed?.message || "북마크 처리 실패"

        if (isUnauthorizedStatus(res.status) || isLoginRequiredMessage(message)) {
          openLoginRequiredPopup("로그인이 필요한 기능입니다.")
          return
        }

        throw new Error(message)
      }

      const data = parsed as BookmarkResponse
      const nextBookmarked = Boolean(data.bookmarked)

      setBookmarked(nextBookmarked)
      onBookmarkToggle?.(nextBookmarked)

      window.dispatchEvent(new CustomEvent("notifications-updated"))
    } catch (error: any) {
      console.error("북마크 처리 실패:", error)
      const message = error?.message || "북마크 처리 실패"

      if (isLoginRequiredMessage(message)) {
        openLoginRequiredPopup("로그인이 필요한 기능입니다.")
        return
      }

      alert(message)
    } finally {
      setBookmarkLoading(false)
    }
  }

  return (
    <div
      className="flex items-center gap-2"
      onClick={(e) => e.stopPropagation()}
    >
      {loginRequiredPopup.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
          <div className="w-full max-w-sm rounded-xl border border-border bg-card p-6 shadow-lg">
            <h3 className="text-lg font-semibold text-foreground">로그인 안내</h3>
            <p className="mt-3 text-sm text-muted-foreground">{loginRequiredPopup.message}</p>
            <div className="mt-6 flex justify-end gap-2">
              <button
                type="button"
                onClick={closeLoginRequiredPopup}
                className="rounded-md border border-border px-4 py-2 text-sm font-medium text-foreground"
              >
                취소
              </button>
              <button
                type="button"
                onClick={moveToLoginPage}
                className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground"
              >
                로그인 하러가기
              </button>
            </div>
          </div>
        </div>
      )}
      <Button
        type="button"
        variant={liked ? "default" : "outline"}
        size="sm"
        onClick={handleLike}
        disabled={likeLoading}
        className="gap-1"
      >
        <Heart className={`h-4 w-4 ${liked ? "fill-current" : ""}`} />
        <span>{likeCount}</span>
      </Button>

      <Button
        type="button"
        variant={bookmarked ? "default" : "outline"}
        size="icon"
        onClick={handleBookmark}
        disabled={bookmarkLoading}
      >
        <Bookmark className={`h-4 w-4 ${bookmarked ? "fill-current" : ""}`} />
        <span className="sr-only">북마크</span>
      </Button>
    </div>
  )
}