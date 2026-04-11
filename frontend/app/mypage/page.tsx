"use client"

import { useState } from "react"
import Link from "next/link"
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

// Mock user data
const userData = {
  name: "김개발",
  username: "kimdev",
  avatar: "",
  bio: "10년차 풀스택 개발자. React, TypeScript, Node.js를 주로 사용합니다. 지속 가능한 코드와 팀 문화에 관심이 많습니다.",
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

// Mock posts
const myPosts: Post[] = [
  {
    id: "1",
    title: "개발자의 번아웃 예방: 나만의 루틴 만들기",
    excerpt: "10년차 개발자가 알려주는 번아웃 예방법. 지속 가능한 개발자 생활을 위한 팁들을 공유합니다.",
    author: { name: "김개발" },
    category: "커리어",
    createdAt: "4시간 전",
    likes: 234,
    comments: 67,
    views: 2890,
    tags: ["career", "burnout", "productivity"],
  },
  {
    id: "2",
    title: "Next.js 16에서 달라진 점들: 실무에서 바로 적용하기",
    excerpt: "Next.js 16이 출시되면서 많은 변화가 있었습니다. 이 글에서는 실무에서 바로 적용할 수 있는 주요 변경사항들을 살펴봅니다.",
    author: { name: "김개발" },
    category: "Next.js",
    createdAt: "2일 전",
    likes: 128,
    comments: 24,
    views: 1520,
    tags: ["nextjs", "react", "frontend"],
  },
]

const bookmarkedPosts: Post[] = [
  {
    id: "3",
    title: "React Server Components 완벽 가이드",
    excerpt: "React Server Components의 동작 원리부터 실제 프로젝트 적용까지.",
    author: { name: "이리액트" },
    category: "React",
    createdAt: "1일 전",
    likes: 256,
    comments: 42,
    views: 3240,
    tags: ["react", "rsc", "server-components"],
  },
]

const likedPosts: Post[] = [
  {
    id: "4",
    title: "TypeScript 5.0 새로운 기능 총정리",
    excerpt: "TypeScript 5.0에서 추가된 새로운 기능들을 예제와 함께 상세히 살펴봅니다.",
    author: { name: "박타입" },
    category: "TypeScript",
    createdAt: "5시간 전",
    likes: 89,
    comments: 15,
    views: 892,
    tags: ["typescript", "javascript", "programming"],
  },
]

export default function MyPage() {
  const [activeTab, setActiveTab] = useState("posts")

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Profile Header */}
      <div className="mb-8 rounded-lg border border-border bg-card p-6">
        <div className="flex flex-col items-start gap-6 sm:flex-row">
          {/* Avatar */}
          <Avatar className="h-24 w-24 border-4 border-primary/20">
            <AvatarImage src={userData.avatar} alt={userData.name} />
            <AvatarFallback className="bg-primary text-2xl text-primary-foreground">
              {userData.name.slice(0, 2)}
            </AvatarFallback>
          </Avatar>

          {/* Info */}
          <div className="flex-1">
            <div className="mb-4 flex flex-wrap items-center gap-4">
              <div>
                <h1 className="text-2xl font-bold text-foreground">{userData.name}</h1>
                <p className="text-muted-foreground">@{userData.username}</p>
              </div>
              <Link href="/mypage/edit">
                <Button variant="outline" className="gap-2">
                  <Settings className="h-4 w-4" />
                  프로필 수정
                </Button>
              </Link>
            </div>

            <p className="mb-4 text-foreground leading-relaxed">{userData.bio}</p>

            {/* Meta */}
            <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
              {userData.location && (
                <div className="flex items-center gap-1">
                  <MapPin className="h-4 w-4" />
                  {userData.location}
                </div>
              )}
              {userData.website && (
                <a
                  href={userData.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1 hover:text-primary"
                >
                  <LinkIcon className="h-4 w-4" />
                  {userData.website.replace("https://", "")}
                </a>
              )}
              <div className="flex items-center gap-1">
                <Calendar className="h-4 w-4" />
                {userData.joinedAt} 가입
              </div>
            </div>

            {/* Social Links */}
            <div className="mt-4 flex gap-3">
              {userData.github && (
                <a
                  href={`https://github.com/${userData.github}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-muted-foreground hover:text-foreground"
                >
                  <Github className="h-5 w-5" />
                </a>
              )}
              {userData.twitter && (
                <a
                  href={`https://twitter.com/${userData.twitter}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-muted-foreground hover:text-foreground"
                >
                  <Twitter className="h-5 w-5" />
                </a>
              )}
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="mt-6 grid grid-cols-4 gap-4 border-t border-border pt-6">
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{userData.stats.posts}</p>
            <p className="text-sm text-muted-foreground">글</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">
              {userData.stats.followers.toLocaleString()}
            </p>
            <p className="text-sm text-muted-foreground">팔로워</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">{userData.stats.following}</p>
            <p className="text-sm text-muted-foreground">팔로잉</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-foreground">
              {userData.stats.likes.toLocaleString()}
            </p>
            <p className="text-sm text-muted-foreground">좋아요</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6 w-full justify-start bg-secondary">
          <TabsTrigger
            value="posts"
            className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <FileText className="h-4 w-4" />
            내 글
          </TabsTrigger>
          <TabsTrigger
            value="bookmarks"
            className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <Bookmark className="h-4 w-4" />
            북마크
          </TabsTrigger>
          <TabsTrigger
            value="likes"
            className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <Heart className="h-4 w-4" />
            좋아요
          </TabsTrigger>
          <TabsTrigger
            value="comments"
            className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <MessageCircle className="h-4 w-4" />
            댓글
          </TabsTrigger>
        </TabsList>

        <TabsContent value="posts">
          {myPosts.length > 0 ? (
            <div className="grid gap-6">
              {myPosts.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<FileText className="h-12 w-12" />}
              title="작성한 글이 없습니다"
              description="첫 번째 글을 작성해보세요!"
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
              description="관심있는 글을 북마크해보세요!"
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
              description="마음에 드는 글에 좋아요를 눌러보세요!"
            />
          )}
        </TabsContent>

        <TabsContent value="comments">
          <EmptyState
            icon={<MessageCircle className="h-12 w-12" />}
            title="작성한 댓글이 없습니다"
            description="글에 댓글을 달아 소통해보세요!"
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
      {action && (
        <Link href={action.href}>
          <Button className="mt-4 bg-primary text-primary-foreground hover:bg-primary/90">
            {action.label}
          </Button>
        </Link>
      )}
    </div>
  )
}
