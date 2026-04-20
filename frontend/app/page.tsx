"use client"

import { useCallback, useEffect, useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import {
  AUTH_CHANGED_EVENT,
  getAccessToken,
} from "@/lib/auth-storage"

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

type PostPageResponse = {
  content: {
    postId: number
    title: string
    content: string
    nickName: string
    categoryId: number
    viewCount: number
    likeCount: number
    commentCount: number
    createdAt: string
    liked: boolean
    bookmarked: boolean
  }[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

function formatTimeAgo(dateString: string) {
  const now = new Date()
  const created = new Date(dateString)
  const diffMs = now.getTime() - created.getTime()

  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffMinutes < 1) return "방금 전"
  if (diffMinutes < 60) return `${diffMinutes}분 전`
  if (diffHours < 24) return `${diffHours}시간 전`
  if (diffDays < 7) return `${diffDays}일 전`

  return created.toLocaleDateString("ko-KR")
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

export default function PostsPage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadPosts = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)

      const response = await fetch(`${API_BASE_URL}/api/posts`, {
        method: "GET",
        credentials: "include",
        headers: getAuthHeaders(),
        cache: "no-store",
      })

      if (!response.ok) {
        throw new Error("게시글 목록을 불러오지 못했습니다.")
      }

      const data: PostPageResponse = await response.json()

      const mapped: Post[] = data.content.map((post) => ({
        id: String(post.postId),
        title: post.title,
        excerpt: post.content,
        author: {
          name: post.nickName,
        },
        category: String(post.categoryId),
        createdAt: formatTimeAgo(post.createdAt),
        likes: post.likeCount,
        comments: post.commentCount,
        views: post.viewCount,
        tags: [],
        liked: Boolean(post.liked),
        bookmarked: Boolean(post.bookmarked),
      }))

      setPosts(mapped)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "알 수 없는 오류가 발생했습니다."
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadPosts()
  }, [loadPosts])

  useEffect(() => {
    const handleAuthChanged = () => {
      void loadPosts()
    }

    window.addEventListener(AUTH_CHANGED_EVENT, handleAuthChanged)
    return () => {
      window.removeEventListener(AUTH_CHANGED_EVENT, handleAuthChanged)
    }
  }, [loadPosts])

  if (loading) {
    return <div className="px-4 py-10">로딩 중...</div>
  }

  if (error) {
    return <div className="px-4 py-10 text-destructive">{error}</div>
  }

  return (
    <main className="mx-auto max-w-6xl px-4 py-10">
      <section className="space-y-4">
        {posts.length === 0 ? (
          <div className="rounded-lg border border-border bg-card p-6 text-center text-muted-foreground">
            게시글이 없습니다.
          </div>
        ) : (
          posts.map((post) => (
            <PostCard
              key={`${post.id}-${post.liked}-${post.bookmarked}-${post.likes}`}
              post={post}
            />
          ))
        )}
      </section>
    </main>
  )
}