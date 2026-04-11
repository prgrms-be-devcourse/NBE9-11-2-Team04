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
  { id: "2", name: "박취준", avatar: "", bio: "취업/이직 전문가" },
  { id: "3", name: "이트렌드", avatar: "", bio: "IT 트렌드 분석가" },
]

// Mock suggested users
const suggestedUsers = [
  { id: "4", name: "정수익", avatar: "", bio: "사이드 프로젝트 전문가", followers: 1234 },
  { id: "5", name: "최데브옵스", avatar: "", bio: "DevOps 엔지니어", followers: 987 },
  { id: "6", name: "강경험", avatar: "", bio: "커리어 멘토", followers: 756 },
]

// Mock feed posts
const feedPosts: Post[] = [
  {
    id: "1",
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
    title: "2026년 프론트엔드 개발자 로드맵: 꼭 알아야 할 기술 스택",
    excerpt: "React, Next.js, TypeScript를 중심으로 2026년 프론트엔드 개발자가 반드시 알아야 할 기술들을 정리했습니다.",
    author: { name: "김개발" },
    category: "IT 기술 정보",
    createdAt: "2일 전",
    likes: 128,
    comments: 24,
    views: 1520,
    tags: ["frontend", "react", "개발로드맵"],
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
