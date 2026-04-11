"use client"

import { useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Users, UserPlus, Search } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import Link from "next/link"

// Mock followed users
const followedUsers = [
  { id: "1", name: "김개발", avatar: "", bio: "10년차 풀스택 개발자" },
  { id: "2", name: "박타입", avatar: "", bio: "TypeScript 러버" },
  { id: "3", name: "이리액트", avatar: "", bio: "React 전문가" },
]

// Mock suggested users
const suggestedUsers = [
  { id: "4", name: "정파이썬", avatar: "", bio: "AI/ML 개발자", followers: 1234 },
  { id: "5", name: "최데브옵스", avatar: "", bio: "DevOps 엔지니어", followers: 987 },
  { id: "6", name: "강상태", avatar: "", bio: "상태관리 전문가", followers: 756 },
]

// Mock feed posts
const feedPosts: Post[] = [
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
    title: "TypeScript 5.0 새로운 기능 총정리",
    excerpt: "TypeScript 5.0에서 추가된 새로운 기능들을 예제와 함께 상세히 살펴봅니다. 데코레이터, const 타입 파라미터 등을 다룹니다.",
    author: { name: "박타입" },
    category: "TypeScript",
    createdAt: "5시간 전",
    likes: 89,
    comments: 15,
    views: 892,
    tags: ["typescript", "javascript", "programming"],
  },
  {
    id: "3",
    title: "React Server Components 완벽 가이드",
    excerpt: "React Server Components의 동작 원리부터 실제 프로젝트 적용까지. RSC를 제대로 이해하고 활용하는 방법을 알아봅니다.",
    author: { name: "이리액트" },
    category: "React",
    createdAt: "1일 전",
    likes: 256,
    comments: 42,
    views: 3240,
    tags: ["react", "rsc", "server-components"],
  },
  {
    id: "4",
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

export default function FeedPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const hasFollowing = followedUsers.length > 0

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="grid gap-8 lg:grid-cols-3">
        {/* Main Content */}
        <div className="lg:col-span-2">
          {/* Header */}
          <div className="mb-8 flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
              <Users className="h-5 w-5 text-primary" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-foreground">피드</h1>
              <p className="text-muted-foreground">즐겨찾기한 사용자의 글을 모아봅니다</p>
            </div>
          </div>

          {/* Feed Content */}
          {hasFollowing && feedPosts.length > 0 ? (
            <div className="grid gap-6">
              {feedPosts.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>
          ) : (
            <div className="rounded-lg border border-border bg-card p-12 text-center">
              <Users className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold text-foreground">
                피드가 비어있습니다
              </h3>
              <p className="mb-6 text-sm text-muted-foreground">
                관심있는 작성자를 즐겨찾기하면 이곳에서 글을 모아볼 수 있습니다.
              </p>
              <Link href="/popular">
                <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                  인기 작성자 둘러보기
                </Button>
              </Link>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Following */}
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="font-semibold text-foreground">즐겨찾기</h2>
              <span className="text-sm text-muted-foreground">
                {followedUsers.length}명
              </span>
            </div>
            {followedUsers.length > 0 ? (
              <div className="space-y-4">
                {followedUsers.map((user) => (
                  <Link
                    key={user.id}
                    href={`/user/${user.name}`}
                    className="flex items-center gap-3 transition-colors hover:opacity-80"
                  >
                    <Avatar className="h-10 w-10">
                      <AvatarImage src={user.avatar} alt={user.name} />
                      <AvatarFallback className="bg-secondary text-secondary-foreground">
                        {user.name.slice(0, 2)}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex-1 overflow-hidden">
                      <p className="truncate font-medium text-foreground">
                        {user.name}
                      </p>
                      <p className="truncate text-sm text-muted-foreground">
                        {user.bio}
                      </p>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-center text-sm text-muted-foreground">
                아직 즐겨찾기한 사용자가 없습니다
              </p>
            )}
          </div>

          {/* Suggested Users */}
          <div className="rounded-lg border border-border bg-card p-6">
            <h2 className="mb-4 font-semibold text-foreground">추천 작성자</h2>
            <div className="mb-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="text"
                  placeholder="작성자 검색..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="bg-secondary pl-9"
                />
              </div>
            </div>
            <div className="space-y-4">
              {suggestedUsers.map((user) => (
                <div key={user.id} className="flex items-center gap-3">
                  <Link href={`/user/${user.name}`} className="flex items-center gap-3 flex-1">
                    <Avatar className="h-10 w-10">
                      <AvatarImage src={user.avatar} alt={user.name} />
                      <AvatarFallback className="bg-secondary text-secondary-foreground">
                        {user.name.slice(0, 2)}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex-1 overflow-hidden">
                      <p className="truncate font-medium text-foreground">
                        {user.name}
                      </p>
                      <p className="truncate text-xs text-muted-foreground">
                        팔로워 {user.followers.toLocaleString()}명
                      </p>
                    </div>
                  </Link>
                  <Button variant="outline" size="sm" className="gap-1">
                    <UserPlus className="h-3 w-3" />
                    <span className="sr-only sm:not-sr-only">팔로우</span>
                  </Button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
