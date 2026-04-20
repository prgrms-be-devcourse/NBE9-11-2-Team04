"use client"

import { useEffect, useState } from "react"
import type React from "react"
import { Heart, Bookmark } from "lucide-react"
import { Button } from "@/components/ui/button"
import { getAccessToken } from "@/lib/auth-storage"

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

type InteractionButtonsProps = {
  postId: number
  initialLiked?: boolean
  initialBookmarked?: boolean
  initialLikeCount?: number
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

export default function InteractionButtons({
  postId,
  initialLiked = false,
  initialBookmarked = false,
  initialLikeCount = 0,
}: InteractionButtonsProps) {
  const [liked, setLiked] = useState(initialLiked)
  const [bookmarked, setBookmarked] = useState(initialBookmarked)
  const [likeCount, setLikeCount] = useState(initialLikeCount)
  const [likeLoading, setLikeLoading] = useState(false)
  const [bookmarkLoading, setBookmarkLoading] = useState(false)

  useEffect(() => {
    setLiked(initialLiked)
  }, [initialLiked])

  useEffect(() => {
    setBookmarked(initialBookmarked)
  }, [initialBookmarked])

  useEffect(() => {
    setLikeCount(initialLikeCount)
  }, [initialLikeCount])

  const handleLike = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault()
    e.stopPropagation()

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
      alert(error.message || "좋아요 처리 실패")
    } finally {
      setLikeLoading(false)
    }
  }

  const handleBookmark = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault()
    e.stopPropagation()

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
        throw new Error(message)
      }

      const data = parsed as BookmarkResponse
      setBookmarked(Boolean(data.bookmarked))

      window.dispatchEvent(new CustomEvent("notifications-updated"))
    } catch (error: any) {
      console.error("북마크 처리 실패:", error)
      alert(error.message || "북마크 처리 실패")
    } finally {
      setBookmarkLoading(false)
    }
  }

  return (
    <div
      className="flex items-center gap-2"
      onClick={(e) => e.stopPropagation()}
    >
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