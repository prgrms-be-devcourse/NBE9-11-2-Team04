"use client"

import { useState } from "react"
import { Heart, Bookmark } from "lucide-react"
import { Button } from "@/components/ui/button"
import { toggleBookmark, toggleLike } from "@/lib/interaction"

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
      const data = await toggleLike(postId, liked)
      setLiked(data.liked)
      setLikeCount(data.likeCount)
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "좋아요 처리에 실패했습니다."

      if (message === "UNAUTHORIZED") {
        alert("로그인이 필요한 기능입니다.")
      } else {
        alert(message)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleBookmark = async () => {
    if (loading) return
    setLoading(true)

    try {
      const data = await toggleBookmark(postId, bookmarked)
      setBookmarked(data.bookmarked)
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "북마크 처리에 실패했습니다."

      if (message === "UNAUTHORIZED") {
        alert("로그인이 필요한 기능입니다.")
      } else {
        alert(message)
      }
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
