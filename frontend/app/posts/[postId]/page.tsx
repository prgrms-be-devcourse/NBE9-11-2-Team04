"use client"

import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
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

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

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

  // 현재 로그인 유저
  const [currentUserId, setCurrentUserId] = useState<number | null>(null)

  const postId = useMemo(() => {
    const rawPostId = params?.postId
    if (Array.isArray(rawPostId)) return Number(rawPostId[0])
    return Number(rawPostId)
  }, [params])

  // 기존 게시글 조회
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
        setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }

    void loadPost()
  }, [postId])

  // 내 정보 조회
  useEffect(() => {
    const loadMe = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/api/users/me`, {
          credentials: "include",
          headers: getAuthHeaders(),
        })

        if (!res.ok) return

        const data = await res.json()
        setCurrentUserId(data.data.userId)
      } catch {
        // ignore
      }
    }

    void loadMe()
  }, [])

  //작성자 여부 확인
  const isAuthor = post?.userId && currentUserId === post.userId

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
          // keep default message
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

  // 추가: 삭제 기능
  const handleDeletePost = async () => {
    if (!postId) return

    if (!confirm("정말 삭제하시겠습니까?")) return

    try {
      const res = await fetch(`${API_BASE_URL}/api/posts/${postId}`, {
        method: "DELETE",
        credentials: "include",
        headers: getAuthHeaders(),
      })

      if (!res.ok) {
        throw new Error("삭제 실패")
      }

      alert("삭제되었습니다.")
      window.location.href = "/"
    } catch (err) {
      alert(err instanceof Error ? err.message : "오류 발생")
    }
  }

  if (!postId || Number.isNaN(postId)) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-10">
        <div className="rounded-xl border border-border bg-card p-6 text-center">
          <h1 className="text-xl font-semibold text-foreground">잘못된 게시글 경로입니다.</h1>
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
            <h1 className="text-2xl font-bold text-foreground">{post?.title}</h1>
            {post?.userId ? (
              <div className="mt-2 text-sm text-muted-foreground">
                작성자:{" "}
                <Link href={`/users/${post.userId}`} className="font-medium text-foreground hover:underline">
                  {post.writerName}
                </Link>
              </div>
            ) : null}
            {/* 조회수, 댓글수 추가 */}
            <div className="mt-1 text-xs text-muted-foreground">
              조회수 {post?.viewCount ?? 0} · 댓글 {post?.commentCount ?? 0}
            </div>

            <div className="mt-6 whitespace-pre-wrap rounded-lg bg-muted/30 p-4 text-sm">{post?.content}</div>

            <div className="mt-6">
              <InteractionButtons
                postId={postId}
                initialLiked={Boolean(post?.liked ?? false)}
                initialBookmarked={Boolean(post?.bookmarked ?? false)}
                initialLikeCount={post?.likeCount ?? 0}
              />
            </div>

            {/* 추가: 수정/삭제 버튼 */}
            {isAuthor && (
              <div className="mt-4 flex items-center gap-2">
              <Link
                href={`/write?postId=${postId}`}
                className="rounded-md border px-4 py-2 text-sm hover:bg-muted">

                수정
              </Link>

              <button
                onClick={handleDeletePost}
                className="rounded-md border border-destructive/40 px-4 py-2 text-sm text-destructive hover:bg-destructive/10"
              >
                삭제
              </button>
              </div>
            )}

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