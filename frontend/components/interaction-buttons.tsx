"use client"

import { useState } from "react"
import { Heart, Bookmark } from "lucide-react"
import { Button } from "@/components/ui/button"
import { getAccessToken } from "@/lib/auth-storage"

const BASE_URL = "http://localhost:8080"

type InteractionButtonsProps = {
  postId: number
  initialLiked?: boolean
  initialBookmarked?: boolean
  initialLikeCount?: number
}

type LikeResponse = {
  liked: boolean
  likeCount: number
  message?: string
}

type BookmarkResponse = {
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

export default function InteractionButtons({
  postId,
  initialLiked = false,
  initialBookmarked = false,
  initialLikeCount = 0,
}: InteractionButtonsProps) {
  const [liked, setLiked] = useState(initialLiked)
  const [bookmarked, setBookmarked] = useState(initialBookmarked)
  const [likeCount, setLikeCount] = useState(initialLikeCount)
  const [loading, setLoading] = useState(false)

  // ✅ 좋아요 토글
  const handleLike = async () => {
    const token = getAccessToken()
    if (!token) {
      alert("로그인이 필요한 서비스입니다.")
      return
    }

    if (loading) return
    setLoading(true)

    try {
      const method = liked ? "DELETE" : "POST"

      const res = await fetch(`${BASE_URL}/api/posts/${postId}/likes`, {
        method,
        headers: {
          "Content-Type": "application/json",
          authorization: `Bearer ${token}`,
        },
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

      // 🔥 서버 기준으로 상태 동기화
      setLiked(data.liked)
      setLikeCount(data.likeCount)
    } catch (error: any) {
      console.error("좋아요 처리 실패:", error)
      alert(error.message || "좋아요 처리 실패")
    } finally {
      setLoading(false)
    }
  }

  // ✅ 북마크 토글
  const handleBookmark = async () => {
    const token = getAccessToken()
    if (!token) {
      alert("로그인이 필요한 서비스입니다.")
      return
    }

    if (loading) return
    setLoading(true)

    try {
      const method = bookmarked ? "DELETE" : "POST"

      const res = await fetch(`${BASE_URL}/api/posts/${postId}/bookmarks`, {
        method,
        headers: {
          "Content-Type": "application/json",
          authorization: `Bearer ${token}`,
        },
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

      // 🔥 서버 기준으로 상태 동기화
      setBookmarked(data.bookmarked)
    } catch (error: any) {
      console.error("북마크 처리 실패:", error)
      alert(error.message || "북마크 처리 실패")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center gap-2">
      <Button
        type="button"
        variant={liked ? "default" : "outline"}
        size="sm"
        onClick={handleLike}
        disabled={loading}
        className="gap-1"
      >
        <Heart className="h-4 w-4" />
        <span>{likeCount}</span>
      </Button>

      <Button
        type="button"
        variant={bookmarked ? "default" : "outline"}
        size="icon"
        onClick={handleBookmark}
        disabled={loading}
      >
        <Bookmark className="h-4 w-4" />
        <span className="sr-only">북마크</span>
      </Button>
    </div>
  )
}