"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { PostCard, type Post } from "@/components/post-card"
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
import {
  AUTH_CHANGED_EVENT,
  getAuthSnapshot,
  getCurrentUserProfile,
} from "@/lib/auth-storage"

const defaultUserData = {
  avatar: "",
  bio: "10년차 풀스택 개발자. React, TypeScript, Node.js를 주로 사용합니다.",
  location: "서울, 대한민국",
  website: "https://kimdev.blog",
  github: "kimdev",
  twitter: "kimdev",
  joinedAt: "2024년 1월",
  stats: {
    posts: 42,
    followers: 1234,
    following: 89,
    likes: 5678,
  },
}

const myPosts: Post[] = [
  {
    id: "1",
    title: "개발자의 번아웃 예방: 나만의 루틴 만들기",
    excerpt:
      "10년차 개발자가 알려주는 번아웃 예방법. 지속 가능한 개발자 생활을 위한 팁들을 공유합니다.",
    author: { name: "김개발" },
    category: "자유 주제",
    createdAt: "4시간 전",
    likes: 234,
    comments: 67,
    views: 2890,
    tags: ["번아웃", "루틴", "개발자생활"],
  },
  {
    id: "2",
    title: "2026년 프론트엔드 개발자 로드맵",
    excerpt:
      "React, Next.js, TypeScript 중심으로 프론트엔드 개발자가 알아야 할 기술들을 정리했습니다.",
    author: { name: "김개발" },
    category: "IT 기술 정보",
    createdAt: "2일 전",
    likes: 128,
    comments: 24,
    views: 1520,
    tags: ["frontend", "react", "로드맵"],
  },
]

const bookmarkedPosts: Post[] = [
  {
    id: "3",
    title: "AI 코딩 어시스턴트 비교: Copilot vs Cursor vs Claude",
    excerpt:
      "개발 생산성을 높여주는 AI 도구들을 실제 사용 경험을 바탕으로 비교 분석합니다.",
    author: { name: "이트렌드" },
    category: "개발자 트렌드",
    createdAt: "1일 전",
    likes: 256,
    comments: 42,
    views: 3240,
    tags: ["ai", "copilot", "개발도구"],
  },
]

const likedPosts: Post[] = [
  {
    id: "4",
    title: "신입 개발자 채용 트렌드 분석",
    excerpt:
      "상반기 IT 기업들의 신입 개발자 채용 동향과 필요한 역량을 분석합니다.",
    author: { name: "박취준" },
    category: "취업 시장 정보",
    createdAt: "5시간 전",
    likes: 89,
    comments: 15,
    views: 892,
    tags: ["취업", "신입채용", "대기업"],
  },
]

export default function MyPage() {
  const router = useRouter()
  const [activeTab, setActiveTab] = useState("posts")
  const [displayName, setDisplayName] = useState("김개발")
  const [displayUsername, setDisplayUsername] = useState("kimdev")
  const [isAuthReady, setIsAuthReady] = useState(false)
  const [profileData, setProfileData] = useState({
    bio: defaultUserData.bio,
    location: defaultUserData.location,
    website: defaultUserData.website,
    github: defaultUserData.github,
    twitter: defaultUserData.twitter,
  })

  useEffect(() => {
    const auth = getAuthSnapshot()
    if (!auth.isLoggedIn) {
      router.replace("/login")
      return
    }

    setIsAuthReady(true)
  }, [router])

  useEffect(() => {
    const syncProfile = () => {
      const auth = getAuthSnapshot()
      const profile = getCurrentUserProfile()
      const fallbackName = "김개발"
      const name = profile?.nickname?.trim() || auth.nickname?.trim() || fallbackName
      const usernameFromEmail = auth.email?.split("@")[0]?.trim()
      const usernameFromProfile = profile?.username?.trim()
      const usernameFromName = name.trim().replace(/\s+/g, "")
      const username = usernameFromProfile || usernameFromEmail || usernameFromName || "kimdev"

      setDisplayName(name)
      setDisplayUsername(username)
      setProfileData({
        bio: profile?.bio || defaultUserData.bio,
        location: profile?.location || defaultUserData.location,
        website: profile?.website || defaultUserData.website,
        github: profile?.github || defaultUserData.github,
        twitter: profile?.twitter || defaultUserData.twitter,
      })
    }

    syncProfile()
    window.addEventListener(AUTH_CHANGED_EVENT, syncProfile as EventListener)
    window.addEventListener("storage", syncProfile)

    return () => {
      window.removeEventListener(AUTH_CHANGED_EVENT, syncProfile as EventListener)
      window.removeEventListener("storage", syncProfile)
    }
  }, [])

  if (!isAuthReady) {
    return null
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
                <p className="text-muted-foreground">@{displayUsername}</p>
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
            <p className="text-2xl font-bold text-foreground">{defaultUserData.stats.posts}</p>
            <p className="text-sm text-muted-foreground">글</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{defaultUserData.stats.followers.toLocaleString()}</p>
            <p className="text-sm text-muted-foreground">팔로워</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{defaultUserData.stats.following}</p>
            <p className="text-sm text-muted-foreground">팔로잉</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{defaultUserData.stats.likes.toLocaleString()}</p>
            <p className="text-sm text-muted-foreground">좋아요</p>
          </div>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6 w-full justify-start bg-secondary">
          <TabsTrigger value="posts" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <FileText className="h-4 w-4" />내 글
          </TabsTrigger>
          <TabsTrigger value="bookmarks" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <Bookmark className="h-4 w-4" />북마크
          </TabsTrigger>
          <TabsTrigger value="likes" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <Heart className="h-4 w-4" />좋아요
          </TabsTrigger>
          <TabsTrigger value="comments" className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
            <MessageCircle className="h-4 w-4" />댓글
          </TabsTrigger>
        </TabsList>

        <TabsContent value="posts">
          {myPosts.length > 0 ? (
            <div className="grid gap-6">
              {myPosts.map((post) => (
                <PostCard key={post.id} post={{ ...post, author: { ...post.author, name: displayName } }} />
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
          {bookmarkedPosts.length > 0 ? (
            <div className="grid gap-6">
              {bookmarkedPosts.map((post) => (
                <PostCard key={post.id} post={post} />
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
          {likedPosts.length > 0 ? (
            <div className="grid gap-6">
              {likedPosts.map((post) => (
                <PostCard key={post.id} post={post} />
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
          <EmptyState
            icon={<MessageCircle className="h-12 w-12" />}
            title="작성한 댓글이 없습니다"
            description="글에 댓글을 남겨 소통해보세요"
          />
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
          <Button className="mt-4 bg-primary text-primary-foreground hover:bg-primary/90">{action.label}</Button>
        </Link>
      ) : null}
    </div>
  )
}
