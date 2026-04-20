"use client"

import { useEffect, useMemo, useState } from "react"
import { useParams } from "next/navigation"
import CommentSection from "@/components/comment/CommentSection"
import InteractionButtons from "@/components/interaction-buttons"
import { getAccessToken } from "@/lib/auth-storage"

type PostDetailResponse = {
  postId: number
  title: string
  content: string
  userId: number
  writerName: string
  categoryId: number
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount?: number
  bookmarked?: boolean
  createdAt: string
  updatedAt: string
  liked?: boolean
}

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

function getAuthHeaders(): HeadersInit {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  }

  const token = getAccessToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  return headers
}

export default function PostDetailPage() {
  const params = useParams()

  const [post, setPost] = useState<PostDetailResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reportLoading, setReportLoading] = useState(false)

  const postId = useMemo(() => {
    const rawPostId = params?.postId
    if (Array.isArray(rawPostId)) return Number(rawPostId[0])
    return Number(rawPostId)
  }, [params])

  useEffect(() => {
    const loadPost = async () => {
      if (!postId || Number.isNaN(postId)) {
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        setError(null)

        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}`, {
          credentials: "include",
          headers: getAuthHeaders(),
          cache: "no-store",
        })

        if (!response.ok) {
          throw new Error("게시글을 불러오지 못했습니다.")
        }

        const data: PostDetailResponse = await response.json()
        setPost(data)
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다."
        )
      } finally {
        setLoading(false)
      }
    }

    void loadPost()
  }, [postId])

  const handleReportPost = async () => {
    if (!postId || Number.isNaN(postId)) {
      return
    }

    try {
      setReportLoading(true)
      setError(null)

      const response = await fetch(`${API_BASE_URL}/api/report/post`, {
        credentials: "include",
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({
          targetId: postId,
          reasonType: "ETC",
          reasonDetail: "게시글 상세 페이지에서 접수한 신고입니다.",
        }),
      })

      if (!response.ok) {
        let message = "게시글 신고에 실패했습니다."

        try {
          const errorData = await response.json()
          message =
            errorData?.message ??
            errorData?.resultMessage ??
            errorData?.msg ??
            message

          if (typeof message === "string" && message.includes("이미 신고")) {
            message = "이미 신고한 게시글입니다."
          }
        } catch {
          // 응답 본문이 JSON이 아니면 기본 메시지를 그대로 사용
        }

        throw new Error(message)
      }

      alert("게시글 신고가 접수되었습니다.")
      window.dispatchEvent(new CustomEvent("notifications-updated"))
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다."
      )
    } finally {
      setReportLoading(false)
    }
  }

  if (!postId || Number.isNaN(postId)) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-10">
        <div className="rounded-xl border border-border bg-card p-6 text-center">
          <h1 className="text-xl font-semibold text-foreground">
            잘못된 게시글 경로입니다.
          </h1>
        </div>
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-10">
      <section className="mb-8 rounded-xl border border-border bg-card p-6 shadow-sm">
        {loading ? (
          <div>로딩 중...</div>
        ) : error ? (
          <div className="text-destructive">{error}</div>
        ) : (
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              {post?.title}
            </h1>

            <div className="mt-6 whitespace-pre-wrap rounded-lg bg-muted/30 p-4 text-sm">
              {post?.content}
            </div>

            <div className="mt-6">
              <InteractionButtons
                postId={postId}
                initialLiked={Boolean(post?.liked ?? false)}
                initialBookmarked={Boolean(post?.bookmarked ?? false)}
                initialLikeCount={post?.likeCount ?? 0}
              />
            </div>

            <button
              type="button"
              onClick={handleReportPost}
              disabled={reportLoading}
              className="mt-4 rounded-md border border-destructive/40 px-4 py-2 text-sm font-medium text-destructive disabled:cursor-not-allowed disabled:opacity-50"
            >
              {reportLoading ? "신고 중..." : "신고"}
            </button>
          </div>
        )}
      </section>

      <CommentSection postId={postId} />
    </main>
  )
}