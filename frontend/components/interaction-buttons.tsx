"use client"

import { useState } from "react"
import { Heart, Bookmark } from "lucide-react"
import { Button } from "@/components/ui/button"

const BASE_URL = "http://localhost:8080"

function getAuthHeaders(): Record<string, string> {
  if (typeof window === "undefined") {
    return {
      "Content-Type": "application/json",
    }
  }

  const token = window.localStorage.getItem("accessToken")

  if (!token) {
    return {
      "Content-Type": "application/json",
    }
  }

  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  }
}

type InteractionButtonsProps = {
  postId: number
  initialLiked?: boolean
  initialBookmarked?: boolean
  initialLikeCount?: number
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

  const handleLike = async () => {
    if (loading) return
    setLoading(true)

    try {
      const method = liked ? "DELETE" : "POST"

      const res = await fetch(`${BASE_URL}/posts/${postId}/likes`, {
        method,
        headers: getAuthHeaders(),
      })

      if (!res.ok) {
        throw new Error("좋아요 처리 실패")
      }

      const data = await res.json()
      setLiked(data.liked)
      setLikeCount(data.likeCount)
    } catch (error) {
      console.error(error)
      alert("좋아요 처리 실패")
    } finally {
      setLoading(false)
    }
  }

  const handleBookmark = async () => {
    if (loading) return
    setLoading(true)

    try {
      const method = bookmarked ? "DELETE" : "POST"

      const res = await fetch(`${BASE_URL}/posts/${postId}/bookmarks`, {
        method,
        headers: getAuthHeaders(),
      })

      if (!res.ok) {
        throw new Error("북마크 처리 실패")
      }

      const data = await res.json()
      setBookmarked(data.bookmarked)
    } catch (error) {
      console.error(error)
      alert("북마크 처리 실패")
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