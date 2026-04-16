"use client"

import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Settings,
  FileText,
  Bookmark,
  Heart,
  MessageCircle,
  Calendar,
  MapPin,
  Link as LinkIcon,
  Github,
  Twitter,
} from "lucide-react"
import { AUTH_CHANGED_EVENT, getAuthSnapshot } from "@/lib/auth-storage"
import { apiFetch } from "@/lib/api"

type MyProfileResponse = {
  userId: number
  email: string
  nickname: string
}

type MyPostResponse = {
  postId: number
  title: string
  likeCount: number
  commentCount: number
  createdAt: string
}

type MyCommentResponse = {
  commentId: number
  postId: number
  content: string
  createdAt: string
}

type LikedPostResponse = {
  postId: number
  title: string
  authorNickname: string
  likeCount: number
  commentCount: number
  createdAt: string
}

type BookmarkedPostResponse = {
  postId: number
  title: string
  authorNickname: string
  likeCount: number
  commentCount: number
  createdAt: string
}

const defaultUserData = {
  avatar: "",
  bio: "개발과 기록을 좋아하는 사용자입니다.",
  location: "서울, 대한민국",
  website: "https://myprofile.com",
  github: "github",
  twitter: "twitter",
  joinedAt: "최근",
}

function formatDate(value: string) {
  if (!value) return "-"
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  })
}

export default function MyPage() {
  const router = useRouter()

  const [activeTab, setActiveTab] = useState("posts")
  const [isAuthReady, setIsAuthReady] = useState(false)
  const [loading, setLoading] = useState(true)
  const [tabLoading, setTabLoading] = useState(false)
  const [error, setError] = useState("")

  const [profile, setProfile] = useState<MyProfileResponse | null>(null)
  const [posts, setPosts] = useState<MyPostResponse[]>([])
  const [comments, setComments] = useState<MyCommentResponse[]>([])
  const [likes, setLikes] = useState<LikedPostResponse[]>([])
  const [bookmarks, setBookmarks] = useState<BookmarkedPostResponse[]>([])

  const [profileData, setProfileData] = useState({
    bio: defaultUserData.bio,
    location: defaultUserData.location,
    website: defaultUserData.website,
    github: defaultUserData.github,
    twitter: defaultUserData.twitter,
  })

  const displayName = profile?.nickname?.trim() || "사용자"
  const displayUsername = useMemo(() => {
    if (profile?.email) {
      return profile.email.split("@")[0]
    }
    return "user"
  }, [profile])

  useEffect(() => {
    const auth = getAuthSnapshot()
    if (!auth.token) {
      router.replace("/login")
      return
    }
    setIsAuthReady(true)
  }, [router])

  useEffect(() => {
    if (!isAuthReady) return

    const syncProfile = () => {
      const auth = getAuthSnapshot()
      setProfileData((prev) => ({
        ...prev,
        bio: prev.bio || defaultUserData.bio,
        location: prev.location || defaultUserData.location,
        website: prev.website || defaultUserData.website,
        github: prev.github || auth.nickname?.trim() || defaultUserData.github,
        twitter: prev.twitter || defaultUserData.twitter,
      }))
    }

    syncProfile()
    window.addEventListener(AUTH_CHANGED_EVENT, syncProfile as EventListener)
    window.addEventListener("storage", syncProfile)

    return () => {
      window.removeEventListener(AUTH_CHANGED_EVENT, syncProfile as EventListener)
      window.removeEventListener("storage", syncProfile)
    }
  }, [isAuthReady])

  useEffect(() => {
    if (!isAuthReady) return

    const fetchInitialData = async () => {
      try {
        setLoading(true)
        setError("")

        const [profileRes, postsRes] = await Promise.all([
          apiFetch<MyProfileResponse>("/api/mypage", {
            method: "GET",
            auth: true,
          }),
          apiFetch<MyPostResponse[]>("/api/mypage/posts", {
            method: "GET",
            auth: true,
          }),
        ])

        setProfile(profileRes)
        setPosts(postsRes ?? [])
      } catch (err) {
        if (err instanceof Error && err.message === "UNAUTHORIZED") {
          router.replace("/login")
          return
        }

        console.error(err)
        setError("마이페이지 정보를 불러오지 못했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchInitialData()
  }, [isAuthReady, router])

  useEffect(() => {
    if (!isAuthReady || activeTab !== "comments" || comments.length > 0) return

    const fetchComments = async () => {
      try {
        setTabLoading(true)
        const res = await apiFetch<MyCommentResponse[]>("/api/mypage/comments", {
          method: "GET",
          auth: true,
        })
        setComments(res ?? [])
      } catch (err) {
        if (err instanceof Error && err.message === "UNAUTHORIZED") {
          router.replace("/login")
          return
        }
        console.error(err)
        setError("내 댓글을 불러오지 못했습니다.")
      } finally {
        setTabLoading(false)
      }
    }

    fetchComments()
  }, [activeTab, comments.length, isAuthReady, router])

  useEffect(() => {
    if (!isAuthReady || activeTab !== "likes" || likes.length > 0) return

    const fetchLikes = async () => {
      try {
        setTabLoading(true)
        const res = await apiFetch<LikedPostResponse[]>("/api/mypage/likes", {
          method: "GET",
          auth: true,
        })
        setLikes(res ?? [])
      } catch (err) {
        if (err instanceof Error && err.message === "UNAUTHORIZED") {
          router.replace("/login")
          return
        }
        console.error(err)
        setError("좋아요 목록을 불러오지 못했습니다.")
      } finally {
        setTabLoading(false)
      }
    }

    fetchLikes()
  }, [activeTab, isAuthReady, likes.length, router])

  useEffect(() => {
    if (!isAuthReady || activeTab !== "bookmarks" || bookmarks.length > 0) return

    const fetchBookmarks = async () => {
      try {
        setTabLoading(true)
        const res = await apiFetch<BookmarkedPostResponse[]>("/api/mypage/bookmarks", {
          method: "GET",
          auth: true,
        })
        setBookmarks(res ?? [])
      } catch (err) {
        if (err instanceof Error && err.message === "UNAUTHORIZED") {
          router.replace("/login")
          return
        }
        console.error(err)
        setError("북마크 목록을 불러오지 못했습니다.")
      } finally {
        setTabLoading(false)
      }
    }

    fetchBookmarks()
  }, [activeTab, bookmarks.length, isAuthReady, router])

  if (!isAuthReady || loading) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-border bg-card p-12 text-center">
          <p className="text-sm text-muted-foreground">로딩 중...</p>
        </div>
      </div>
    )
  }

  if (error && !profile) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-border bg-card p-12 text-center">
          <p className="text-sm text-red-500">{error}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 rounded-lg border border-border bg-card p-6">
        <div className="flex flex-col items-start gap-6 sm:flex-row">
          <Avatar className="h-24 w-24 border-4 border-primary/20">
            <AvatarImage src={defaultUserData.avatar} alt={displayName} />
            <AvatarFallback className="bg-primary text-2xl text-primary-foreground">
              {displayName.slice(0, 2)}
            </AvatarFallback>
          </Avatar>

          <div className="flex-1">
            <div className="mb-4 flex flex-wrap items-center gap-4">
              <div>
                <h1 className="text-2xl font-bold text-foreground">{displayName}</h1>
                <p className="text-sm text-muted-foreground">@{displayUsername}</p>
              </div>

              <Link href="/mypage/edit">
                <Button variant="outline" className="gap-2">
                  <Settings className="h-4 w-4" />
                  프로필 수정
                </Button>
              </Link>
            </div>

            <p className="mb-4 leading-relaxed text-foreground">{profileData.bio}</p>

            <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
              <div className="flex items-center gap-1">
                <MapPin className="h-4 w-4" />
                {profileData.location}
              </div>

              <a
                href={profileData.website}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 hover:text-primary"
              >
                <LinkIcon className="h-4 w-4" />
                {profileData.website.replace("https://", "")}
              </a>

              <div className="flex items-center gap-1">
                <Calendar className="h-4 w-4" />
                {defaultUserData.joinedAt} 가입
              </div>
            </div>

            <div className="mt-4 flex gap-3">
              <a
                href={`https://github.com/${profileData.github}`}
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground hover:text-foreground"
              >
                <Github className="h-5 w-5" />
              </a>

              <a
                href={`https://twitter.com/${profileData.twitter}`}
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground hover:text-foreground"
              >
                <Twitter className="h-5 w-5" />
              </a>
            </div>
          </div>
        </div>

        <div className="mt-6 grid grid-cols-4 gap-4 border-t border-border pt-6">
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{posts.length}</p>
            <p className="text-sm text-muted-foreground">글</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{bookmarks.length}</p>
            <p className="text-sm text-muted-foreground">북마크</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{likes.length}</p>
            <p className="text-sm text-muted-foreground">좋아요</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{comments.length}</p>
            <p className="text-sm text-muted-foreground">댓글</p>
          </div>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6 w-full justify-start bg-secondary">
          <TabsTrigger value="posts" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <FileText className="h-4 w-4" />
            내 글
          </TabsTrigger>
          <TabsTrigger value="bookmarks" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <Bookmark className="h-4 w-4" />
            북마크
          </TabsTrigger>
          <TabsTrigger value="likes" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <Heart className="h-4 w-4" />
            좋아요
          </TabsTrigger>
          <TabsTrigger value="comments" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <MessageCircle className="h-4 w-4" />
            댓글
          </TabsTrigger>
        </TabsList>

        {tabLoading && (
          <div className="mb-6 rounded-lg border border-border bg-card p-4 text-sm text-muted-foreground">
            불러오는 중...
          </div>
        )}

        <TabsContent value="posts">
          {posts.length > 0 ? (
            <div className="grid gap-6">
              {posts.map((post) => (
                <div key={post.postId} className="rounded-lg border border-border bg-card p-6">
                  <div className="mb-3 flex items-start justify-between gap-4">
                    <div>
                      <h3 className="text-lg font-semibold text-foreground">{post.title}</h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        작성일 {formatDate(post.createdAt)}
                      </p>
                    </div>
                  </div>

                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>좋아요 {post.likeCount}</span>
                    <span>댓글 {post.commentCount}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<FileText className="h-12 w-12" />}
              title="작성한 글이 없습니다"
              description="첫 번째 글을 작성해보세요"
              action={{ label: "글 쓰기", href: "/write" }}
            />
          )}
        </TabsContent>

        <TabsContent value="bookmarks">
          {bookmarks.length > 0 ? (
            <div className="grid gap-6">
              {bookmarks.map((post) => (
                <div key={post.postId} className="rounded-lg border border-border bg-card p-6">
                  <div className="mb-3 flex items-start justify-between gap-4">
                    <div>
                      <h3 className="text-lg font-semibold text-foreground">{post.title}</h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        작성자 {post.authorNickname} · {formatDate(post.createdAt)}
                      </p>
                    </div>
                  </div>

                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>좋아요 {post.likeCount}</span>
                    <span>댓글 {post.commentCount}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<Bookmark className="h-12 w-12" />}
              title="북마크한 글이 없습니다"
              description="관심 있는 글을 북마크해보세요"
            />
          )}
        </TabsContent>

        <TabsContent value="likes">
          {likes.length > 0 ? (
            <div className="grid gap-6">
              {likes.map((post) => (
                <div key={post.postId} className="rounded-lg border border-border bg-card p-6">
                  <div className="mb-3 flex items-start justify-between gap-4">
                    <div>
                      <h3 className="text-lg font-semibold text-foreground">{post.title}</h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        작성자 {post.authorNickname} · {formatDate(post.createdAt)}
                      </p>
                    </div>
                  </div>

                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>좋아요 {post.likeCount}</span>
                    <span>댓글 {post.commentCount}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<Heart className="h-12 w-12" />}
              title="좋아요한 글이 없습니다"
              description="마음에 드는 글에 좋아요를 눌러보세요"
            />
          )}
        </TabsContent>

        <TabsContent value="comments">
          {comments.length > 0 ? (
            <div className="grid gap-6">
              {comments.map((comment) => (
                <div key={comment.commentId} className="rounded-lg border border-border bg-card p-6">
                  <div className="mb-3 flex items-start justify-between gap-4">
                    <div>
                      <h3 className="text-base font-semibold text-foreground">
                        게시글 ID {comment.postId}
                      </h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        작성일 {formatDate(comment.createdAt)}
                      </p>
                    </div>
                  </div>

                  <p className="text-sm leading-6 text-foreground">{comment.content}</p>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<MessageCircle className="h-12 w-12" />}
              title="작성한 댓글이 없습니다"
              description="글에 댓글을 남겨 소통해보세요"
            />
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}

function EmptyState({
  icon,
  title,
  description,
  action,
}: {
  icon: React.ReactNode
  title: string
  description: string
  action?: { label: string; href: string }
}) {
  return (
    <div className="rounded-lg border border-border bg-card p-12 text-center">
      <div className="mx-auto mb-4 text-muted-foreground">{icon}</div>
      <h3 className="mb-2 text-lg font-semibold text-foreground">{title}</h3>
      <p className="text-sm text-muted-foreground">{description}</p>
      {action ? (
        <Link href={action.href}>
          <Button className="mt-4 bg-primary text-primary-foreground hover:bg-primary/90">
            {action.label}
          </Button>
        </Link>
      ) : null}
    </div>
  )
}