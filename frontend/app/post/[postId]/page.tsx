"use client"

import { useEffect, useMemo, useState } from "react"
import { useParams } from "next/navigation"
import CommentSection from "@/components/comment/CommentSection"

type PostDetailResponse = {
  id?: number | string
  postId?: number | string
  title?: string
  content?: string
  authorName?: string
  nickname?: string
  memberName?: string
  writerName?: string
  author?: {
    name?: string
    nickname?: string
  }
  createdAt?: string
  likeCount?: number
  liked?: boolean
  isLiked?: boolean
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

function getAuthHeaders(): HeadersInit {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  }

  if (typeof window !== "undefined") {
    const token = window.localStorage.getItem("accessToken")
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  return headers
}

export default function PostDetailPage() {
  const params = useParams()
  const [post, setPost] = useState<PostDetailResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [liked, setLiked] = useState(false)
  const [likeCount, setLikeCount] = useState(0)
  const [likeLoading, setLikeLoading] = useState(false)

  const postId = useMemo(() => {
    const rawPostId = params?.postId
    if (Array.isArray(rawPostId)) {
      return Number(rawPostId[0])
    }
    return Number(rawPostId)
  }, [params])

  const authorDisplayName =
    post?.authorName ??
    post?.nickname ??
    post?.memberName ??
    post?.writerName ??
    post?.author?.name ??
    post?.author?.nickname ??
    "작성자 없음"

  useEffect(() => {
    const loadPost = async () => {
      if (!postId || Number.isNaN(postId)) {
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError(null)

        const endpoints = [`${API_BASE_URL}/api/posts/${postId}`, `${API_BASE_URL}/api/v1/posts/${postId}`]
        let response: Response | null = null

        for (const endpoint of endpoints) {
          const candidate = await fetch(endpoint, { headers: getAuthHeaders() })
          if (candidate.ok) {
            response = candidate
            break
          }
          if (candidate.status !== 404) {
            response = candidate
            break
          }
        }

        if (!response || !response.ok) {
          throw new Error("게시글을 불러오지 못했습니다.")
        }

        const data = await response.json()
        setPost(data)
        setLiked(Boolean(data?.isLiked ?? data?.liked ?? false))
        setLikeCount(typeof data?.likeCount === "number" ? data.likeCount : 0)
      } catch (err) {
        setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }

    void loadPost()
  }, [postId])

  const handleToggleLike = async () => {
    if (!postId || Number.isNaN(postId)) {
      return
    }

    try {
      setLikeLoading(true)
      setError(null)

      const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/likes`, {
        method: liked ? "DELETE" : "POST",
        headers: getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error("좋아요 처리에 실패했습니다.")
      }

      const data = await response.json()
      setLiked(Boolean(data?.liked))
      setLikeCount(typeof data?.likeCount === "number" ? data.likeCount : 0)
      window.dispatchEvent(new CustomEvent("notifications-updated"))
    } catch (err) {
      setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
    } finally {
      setLikeLoading(false)
    }
  }

  if (!postId || Number.isNaN(postId)) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-10">
        <div className="rounded-xl border border-border bg-card p-6 text-center">
          <h1 className="text-xl font-semibold text-foreground">잘못된 게시글 경로입니다.</h1>
          <p className="mt-2 text-sm text-muted-foreground">게시글 번호를 다시 확인해주세요.</p>
        </div>
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-10">
      <section className="mb-8 rounded-xl border border-border bg-card p-6 shadow-sm">
        {loading ? (
          <div>
            <h1 className="text-2xl font-bold text-foreground">게시글 상세</h1>
            <p className="mt-3 text-sm text-muted-foreground">게시글을 불러오는 중입니다...</p>
          </div>
        ) : error ? (
          <div>
            <h1 className="text-2xl font-bold text-foreground">게시글 상세</h1>
            <p className="mt-3 text-sm text-destructive">{error}</p>
            <p className="mt-2 text-sm text-muted-foreground">게시글 ID: {postId}</p>
          </div>
        ) : (
          <div>
            <h1 className="text-2xl font-bold text-foreground">{post?.title ?? "제목 없음"}</h1>
            <div className="mt-3 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
              <span>게시글 ID: {postId}</span>
              <span>작성자: {authorDisplayName}</span>
              {post?.createdAt ? <span>작성일: {post.createdAt}</span> : null}
            </div>
            <div className="mt-6 whitespace-pre-wrap rounded-lg bg-muted/30 p-4 text-sm leading-7 text-foreground">
              {post?.content ?? "내용이 없습니다."}
            </div>
            <div className="mt-6 flex items-center gap-3">
              <button
                type="button"
                onClick={handleToggleLike}
                disabled={likeLoading}
                className="rounded-md border border-border px-4 py-2 text-sm font-medium text-foreground disabled:cursor-not-allowed disabled:opacity-50"
              >
                {likeLoading ? "처리 중..." : liked ? "좋아요 취소" : "좋아요"}
              </button>
              <span className="text-sm text-muted-foreground">좋아요 {likeCount}</span>
            </div>
          </div>
        )}
      </section>

      <CommentSection postId={postId} />
    </main>
  )
}