/* eslint-disable */
"use client"

import { useEffect, useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { TrendingUp, Clock, Users } from "lucide-react"

const API_BASE_URL = "http://localhost:8080"
const TEST_POST_ID = 1

function dispatchNotificationsUpdated() {
  if (typeof window === "undefined") {
    return
  }

  window.dispatchEvent(new CustomEvent("notifications-updated"))
}

function triggerNotificationRefresh() {
  dispatchNotificationsUpdated()

  window.setTimeout(() => {
    dispatchNotificationsUpdated()
  }, 150)
}

type CommentItem = {
  commentId: number
  postId: number
  userId: number
  nickname: string | null
  parentCommentId: number | null
  content: string
  createdAt: string
  updatedAt: string
  deleted: boolean
}

type CommentListResponse = {
  comments: CommentItem[]
}

type NotificationItem = {
  notificationId: number
  userId: number
  actorUserId: number
  postId: number | null
  commentId: number | null
  type: string
  message: string
  isRead?: boolean
  read?: boolean
  createdAt: string
}

type NotificationListResponse = {
  notifications: NotificationItem[]
}

function normalizeNotification(notification: NotificationItem): NotificationItem {
  return {
    ...notification,
    isRead: notification.isRead ?? notification.read ?? false,
  }
}

// Mock data for demonstration
const mockPosts: Post[] = [
  {
    id: "1",
    title: "2026년 프론트엔드 개발자 로드맵: 꼭 알아야 할 기술 스택",
    excerpt: "React, Next.js, TypeScript를 중심으로 2026년 프론트엔드 개발자가 반드시 알아야 할 기술들을 정리했습니다.",
    author: { name: "김개발" },
    category: "IT 기술 정보",
    createdAt: "2시간 전",
    likes: 128,
    comments: 24,
    views: 1520,
    tags: ["frontend", "react", "개발로드맵"],
  },
  {
    id: "2",
    title: "네카라쿠배당토 신입 개발자 채용 트렌드 분석",
    excerpt: "2026년 상반기 대기업 IT 기업들의 신입 개발자 채용 동향과 필요한 역량을 분석합니다.",
    author: { name: "박취준" },
    category: "취업 시장 정보",
    createdAt: "5시간 전",
    likes: 89,
    comments: 15,
    views: 892,
    tags: ["취업", "신입채용", "대기업"],
  },
  {
    id: "3",
    title: "AI 코딩 어시스턴트 비교: Copilot vs Cursor vs Claude",
    excerpt: "개발 생산성을 높여주는 AI 코딩 도구들을 실제 사용 경험을 바탕으로 비교 분석합니다.",
    author: { name: "이트렌드" },
    category: "개발자 트렌드",
    createdAt: "1일 전",
    likes: 256,
    comments: 42,
    views: 3240,
    tags: ["ai", "copilot", "개발도구"],
  },
  {
    id: "4",
    title: "Kubernetes 입문부터 실전까지: 완벽 가이드",
    excerpt: "컨테이너 오케스트레이션의 표준 Kubernetes를 처음부터 실전 적용까지 단계별로 설명합니다.",
    author: { name: "최데브옵스" },
    category: "IT 기술 정보",
    createdAt: "2일 전",
    likes: 178,
    comments: 31,
    views: 2150,
    tags: ["kubernetes", "devops", "인프라"],
  },
  {
    id: "5",
    title: "개발자 사이드 프로젝트로 월 100만원 벌기",
    excerpt: "본업 외에 사이드 프로젝트로 수익을 창출하는 방법과 실제 경험담을 공유합니다.",
    author: { name: "정수익" },
    category: "자유 주제",
    createdAt: "3일 전",
    likes: 312,
    comments: 58,
    views: 4580,
    tags: ["사이드프로젝트", "수익화", "개발자"],
  },
]

const latestPosts: Post[] = [
  {
    id: "6",
    title: "스타트업 vs 대기업: 3년차 개발자의 솔직한 비교",
    excerpt: "스타트업과 대기업 모두 경험해본 개발자가 각각의 장단점을 솔직하게 비교합니다.",
    author: { name: "강경험" },
    category: "자유 주제",
    createdAt: "30분 전",
    likes: 12,
    comments: 3,
    views: 145,
    tags: ["커리어", "스타트업", "대기업"],
  },
  {
    id: "7",
    title: "2026년 개발자 연봉 협상 가이드",
    excerpt: "연봉 협상 시 알아야 할 팁과 실제 연봉 데이터를 기반으로 한 협상 전략을 공유합니다.",
    author: { name: "윤연봉" },
    category: "취업 시장 정보",
    createdAt: "1시간 전",
    likes: 8,
    comments: 2,
    views: 98,
    tags: ["연봉", "이직", "협상"],
  },
  ...mockPosts.slice(0, 3),
]

const feedPosts: Post[] = [
  {
    id: "8",
    title: "개발자의 번아웃 예방: 나만의 루틴 만들기",
    excerpt: "10년차 개발자가 알려주는 번아웃 예방법. 지속 가능한 개발자 생활을 위한 팁들을 공유합니다.",
    author: { name: "김개발" },
    category: "자유 주제",
    createdAt: "4시간 전",
    likes: 234,
    comments: 67,
    views: 2890,
    tags: ["번아웃", "루틴", "개발자생활"],
  },
  {
    id: "9",
    title: "효과적인 코드 리뷰 문화 정착시키기",
    excerpt: "팀에 코드 리뷰 문화를 정착시킨 경험을 공유합니다. 효과적인 코드 리뷰를 위한 가이드라인과 팁을 알려드립니다.",
    author: { name: "박취준" },
    category: "개발자 트렌드",
    createdAt: "1일 전",
    likes: 156,
    comments: 28,
    views: 1890,
    tags: ["코드리뷰", "팀문화", "협업"],
  },
]

export default function HomePage() {
  const [activeTab, setActiveTab] = useState("popular")

  const [comments, setComments] = useState<CommentItem[]>([])
  const [newComment, setNewComment] = useState("")
  const [replyInputs, setReplyInputs] = useState<Record<number, string>>({})
  const [editInputs, setEditInputs] = useState<Record<number, string>>({})
  const [loadingComments, setLoadingComments] = useState(false)
  const [commentError, setCommentError] = useState<string | null>(null)
  const [notifications, setNotifications] = useState<NotificationItem[]>([])

  const loadComments = async () => {
    try {
      setLoadingComments(true)
      setCommentError(null)

      const response = await fetch(`${API_BASE_URL}/api/posts/${TEST_POST_ID}/comments`, {
        cache: "no-store",
      })

      if (!response.ok) {
        throw new Error("댓글 목록을 불러오지 못했습니다.")
      }

      const data: CommentListResponse = await response.json()
      setComments(data.comments)
    } catch (error) {
      const message = error instanceof Error ? error.message : "댓글 요청 중 오류가 발생했습니다."
      setCommentError(message)
    } finally {
      setLoadingComments(false)
    }
  }

  const loadNotifications = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/notifications`, {
        cache: "no-store",
      })

      if (!response.ok) {
        return
      }

      const data: NotificationListResponse = await response.json()
      setNotifications(data.notifications.map(normalizeNotification))
      dispatchNotificationsUpdated()
    } catch {
      // 임시 테스트 화면에서는 알림 로드 실패를 별도 노출하지 않음
    }
  }

  useEffect(() => {
    void loadComments()
    void loadNotifications()
  }, [])

  const handleCreateComment = async () => {
    if (!newComment.trim()) return

    try {
      setCommentError(null)

      const response = await fetch(`${API_BASE_URL}/api/posts/${TEST_POST_ID}/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content: newComment.trim() }),
      })

      if (!response.ok) {
        throw new Error("댓글 작성에 실패했습니다.")
      }

      setNewComment("")
      await loadComments()
      await loadNotifications()
      triggerNotificationRefresh()
    } catch (error) {
      const message = error instanceof Error ? error.message : "댓글 작성 중 오류가 발생했습니다."
      setCommentError(message)
    }
  }

  const handleCreateReply = async (commentId: number) => {
    const value = replyInputs[commentId]?.trim()
    if (!value) return

    try {
      setCommentError(null)

      const response = await fetch(`${API_BASE_URL}/api/comments/${commentId}/replies`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content: value }),
      })

      if (!response.ok) {
        throw new Error("대댓글 작성에 실패했습니다.")
      }

      setReplyInputs((prev) => ({ ...prev, [commentId]: "" }))
      await loadComments()
      await loadNotifications()
      triggerNotificationRefresh()
    } catch (error) {
      const message = error instanceof Error ? error.message : "대댓글 작성 중 오류가 발생했습니다."
      setCommentError(message)
    }
  }

  const handleUpdateComment = async (commentId: number, currentContent: string) => {
    const value = (editInputs[commentId] ?? currentContent).trim()
    if (!value) return

    try {
      setCommentError(null)

      const response = await fetch(`${API_BASE_URL}/api/comments/${commentId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content: value }),
      })

      if (!response.ok) {
        throw new Error("댓글 수정에 실패했습니다.")
      }

      await loadComments()
    } catch (error) {
      const message = error instanceof Error ? error.message : "댓글 수정 중 오류가 발생했습니다."
      setCommentError(message)
    }
  }

  const handleDeleteComment = async (commentId: number) => {
    try {
      setCommentError(null)

      const response = await fetch(`${API_BASE_URL}/api/comments/${commentId}`, {
        method: "DELETE",
      })

      if (!response.ok) {
        throw new Error("댓글 삭제에 실패했습니다.")
      }

      await loadComments()
      await loadNotifications()
      triggerNotificationRefresh()
    } catch (error) {
      const message = error instanceof Error ? error.message : "댓글 삭제 중 오류가 발생했습니다."
      setCommentError(message)
    }
  }

  const topLevelComments = comments.filter((comment) => comment.parentCommentId === null)
  const getReplies = (commentId: number) => comments.filter((comment) => comment.parentCommentId === commentId)

  const getPostsForTab = () => {
    switch (activeTab) {
      case "popular":
        return mockPosts
      case "latest":
        return latestPosts
      case "feed":
        return feedPosts
      default:
        return mockPosts
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Hero Section */}
      <section className="mb-12 text-center">
        <h1 className="mb-4 text-4xl font-bold tracking-tight text-foreground sm:text-5xl">
          개발자들의 <span className="text-primary">지식 허브</span>
        </h1>
        <p className="mx-auto max-w-2xl text-lg leading-relaxed text-muted-foreground">
          최신 기술 트렌드, 실무 경험, 개발 노하우를 함께 나눠보세요.
          <br />
          함께 성장하는 개발자 커뮤니티입니다.
        </p>
      </section>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="mb-8 grid w-full max-w-md mx-auto grid-cols-3 bg-secondary">
          <TabsTrigger
            value="popular"
            className="flex items-center gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <TrendingUp className="h-4 w-4" />
            <span className="hidden sm:inline">인기글</span>
          </TabsTrigger>
          <TabsTrigger
            value="latest"
            className="flex items-center gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <Clock className="h-4 w-4" />
            <span className="hidden sm:inline">최신글</span>
          </TabsTrigger>
          <TabsTrigger
            value="feed"
            className="flex items-center gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <Users className="h-4 w-4" />
            <span className="hidden sm:inline">피드</span>
          </TabsTrigger>
        </TabsList>

        <TabsContent value="popular" className="mt-0">
          <div className="grid gap-6">
            {getPostsForTab().map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        </TabsContent>

        <TabsContent value="latest" className="mt-0">
          <div className="grid gap-6">
            {getPostsForTab().map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        </TabsContent>

        <TabsContent value="feed" className="mt-0">
          {feedPosts.length > 0 ? (
            <div className="grid gap-6">
              {getPostsForTab().map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>
          ) : (
            <div className="rounded-lg border border-border bg-card p-12 text-center">
              <Users className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold text-foreground">
                즐겨찾기한 사용자가 없습니다
              </h3>
              <p className="text-sm text-muted-foreground">
                관심있는 작성자를 즐겨찾기하면 이곳에서 글을 모아볼 수 있습니다.
              </p>
            </div>
          )}
        </TabsContent>
      </Tabs>
      <section className="mt-12 rounded-xl border border-border bg-card p-6 shadow-sm">
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-2xl font-bold text-foreground">댓글 기능 임시 테스트</h2>
            <p className="text-sm text-muted-foreground">
              현재 백엔드 댓글 API를 프론트 화면에서 바로 확인하는 임시 영역입니다. 게시글 ID는 {TEST_POST_ID}로 고정되어 있습니다.
            </p>
          </div>
          <button
            type="button"
            onClick={() => {
              void loadComments()
              void loadNotifications()
            }}
            className="rounded-md border border-border px-4 py-2 text-sm font-medium text-foreground transition hover:bg-secondary"
          >
            새로고침
          </button>
        </div>

        <div className="mb-6 rounded-lg border border-border bg-background p-4">
          <label className="mb-2 block text-sm font-medium text-foreground">댓글 작성</label>
          <div className="flex flex-col gap-3 sm:flex-row">
            <input
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="댓글 내용을 입력하세요"
              className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm outline-none ring-0 placeholder:text-muted-foreground"
            />
            <button
              type="button"
              onClick={() => void handleCreateComment()}
              className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition hover:opacity-90"
            >
              댓글 등록
            </button>
          </div>
        </div>

        {commentError ? (
          <div className="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {commentError}
          </div>
        ) : null}

        {loadingComments ? (
          <div className="rounded-lg border border-dashed border-border p-8 text-center text-sm text-muted-foreground">
            댓글을 불러오는 중입니다...
          </div>
        ) : topLevelComments.length > 0 ? (
          <div className="space-y-4">
            {topLevelComments.map((comment) => {
              const replies = getReplies(comment.commentId)
              const editValue = editInputs[comment.commentId] ?? comment.content
              const replyValue = replyInputs[comment.commentId] ?? ""

              return (
                <div key={comment.commentId} className="rounded-lg border border-border bg-background p-4">
                  <div className="mb-2 flex items-center justify-between gap-3">
                    <div>
                      <p className="text-sm font-semibold text-foreground">
                        댓글 #{comment.commentId}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        userId: {comment.userId} · {comment.createdAt}
                      </p>
                    </div>
                    {comment.deleted ? (
                      <span className="rounded-full bg-secondary px-2 py-1 text-xs text-muted-foreground">
                        삭제됨
                      </span>
                    ) : null}
                  </div>

                  <p className="mb-3 text-sm leading-relaxed text-foreground">{comment.content}</p>

                  <div className="mb-4 flex flex-col gap-2 sm:flex-row">
                    <input
                      value={editValue}
                      onChange={(e) =>
                        setEditInputs((prev) => ({
                          ...prev,
                          [comment.commentId]: e.target.value,
                        }))
                      }
                      className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm outline-none ring-0 placeholder:text-muted-foreground"
                    />
                    <button
                      type="button"
                      onClick={() => void handleUpdateComment(comment.commentId, comment.content)}
                      className="rounded-md border border-border px-3 py-2 text-sm font-medium text-foreground transition hover:bg-secondary"
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      onClick={() => void handleDeleteComment(comment.commentId)}
                      className="rounded-md border border-destructive/40 px-3 py-2 text-sm font-medium text-destructive transition hover:bg-destructive/10"
                    >
                      삭제
                    </button>
                  </div>

                  <div className="rounded-md bg-secondary/40 p-3">
                    <p className="mb-2 text-xs font-semibold text-muted-foreground">대댓글 작성</p>
                    <div className="flex flex-col gap-2 sm:flex-row">
                      <input
                        value={replyValue}
                        onChange={(e) =>
                          setReplyInputs((prev) => ({
                            ...prev,
                            [comment.commentId]: e.target.value,
                          }))
                        }
                        placeholder="대댓글 내용을 입력하세요"
                        className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm outline-none ring-0 placeholder:text-muted-foreground"
                      />
                      <button
                        type="button"
                        onClick={() => void handleCreateReply(comment.commentId)}
                        className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition hover:opacity-90"
                      >
                        대댓글 등록
                      </button>
                    </div>
                  </div>

                  {replies.length > 0 ? (
                    <div className="mt-4 space-y-3 border-l-2 border-border pl-4">
                      {replies.map((reply) => {
                        const replyEditValue = editInputs[reply.commentId] ?? reply.content

                        return (
                          <div key={reply.commentId} className="rounded-md border border-border bg-card p-3">
                            <div className="mb-2 flex items-center justify-between gap-3">
                              <div>
                                <p className="text-sm font-semibold text-foreground">
                                  대댓글 #{reply.commentId}
                                </p>
                                <p className="text-xs text-muted-foreground">
                                  userId: {reply.userId} · {reply.createdAt}
                                </p>
                              </div>
                              {reply.deleted ? (
                                <span className="rounded-full bg-secondary px-2 py-1 text-xs text-muted-foreground">
                                  삭제됨
                                </span>
                              ) : null}
                            </div>

                            <p className="mb-3 text-sm text-foreground">{reply.content}</p>

                            <div className="flex flex-col gap-2 sm:flex-row">
                              <input
                                value={replyEditValue}
                                onChange={(e) =>
                                  setEditInputs((prev) => ({
                                    ...prev,
                                    [reply.commentId]: e.target.value,
                                  }))
                                }
                                className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm outline-none ring-0 placeholder:text-muted-foreground"
                              />
                              <button
                                type="button"
                                onClick={() => void handleUpdateComment(reply.commentId, reply.content)}
                                className="rounded-md border border-border px-3 py-2 text-sm font-medium text-foreground transition hover:bg-secondary"
                              >
                                수정
                              </button>
                              <button
                                type="button"
                                onClick={() => void handleDeleteComment(reply.commentId)}
                                className="rounded-md border border-destructive/40 px-3 py-2 text-sm font-medium text-destructive transition hover:bg-destructive/10"
                              >
                                삭제
                              </button>
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  ) : null}
                </div>
              )
            })}
          </div>
        ) : (
          <div className="rounded-lg border border-dashed border-border p-8 text-center text-sm text-muted-foreground">
            아직 댓글이 없습니다. 위 입력창에서 첫 댓글을 만들어보세요.
          </div>
        )}
      </section>
    </div>
  )
}
