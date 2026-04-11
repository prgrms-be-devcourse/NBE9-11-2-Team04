"use client"

import { useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { TrendingUp, Clock, Users } from "lucide-react"

// Mock data for demonstration
const mockPosts: Post[] = [
  {
    id: "1",
    title: "Next.js 16에서 달라진 점들: 실무에서 바로 적용하기",
    excerpt: "Next.js 16이 출시되면서 많은 변화가 있었습니다. 이 글에서는 실무에서 바로 적용할 수 있는 주요 변경사항들을 살펴봅니다.",
    author: { name: "김개발" },
    category: "Next.js",
    createdAt: "2시간 전",
    likes: 128,
    comments: 24,
    views: 1520,
    tags: ["nextjs", "react", "frontend"],
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
    title: "Docker와 Kubernetes로 개발 환경 구축하기",
    excerpt: "로컬 개발부터 프로덕션 배포까지, 컨테이너 기반의 개발 환경을 구축하는 방법을 단계별로 설명합니다.",
    author: { name: "최데브옵스" },
    category: "DevOps",
    createdAt: "2일 전",
    likes: 178,
    comments: 31,
    views: 2150,
    tags: ["docker", "kubernetes", "devops"],
  },
  {
    id: "5",
    title: "Python으로 만드는 AI 챗봇: GPT API 활용법",
    excerpt: "OpenAI GPT API를 활용하여 나만의 AI 챗봇을 만드는 방법을 소개합니다. 프롬프트 엔지니어링 팁도 함께 다룹니다.",
    author: { name: "정파이썬" },
    category: "AI/ML",
    createdAt: "3일 전",
    likes: 312,
    comments: 58,
    views: 4580,
    tags: ["python", "ai", "gpt", "chatbot"],
  },
]

const latestPosts: Post[] = [
  {
    id: "6",
    title: "Zustand vs Jotai: 상태관리 라이브러리 비교",
    excerpt: "가벼운 상태관리 라이브러리로 주목받는 Zustand와 Jotai를 비교 분석합니다. 각각의 장단점과 적합한 사용 사례를 알아봅니다.",
    author: { name: "강상태" },
    category: "React",
    createdAt: "30분 전",
    likes: 12,
    comments: 3,
    views: 145,
    tags: ["react", "state-management", "zustand", "jotai"],
  },
  {
    id: "7",
    title: "CSS Grid 마스터하기: 실전 레이아웃 예제",
    excerpt: "CSS Grid를 활용한 다양한 실전 레이아웃 예제를 살펴봅니다. 복잡한 레이아웃도 쉽게 구현할 수 있습니다.",
    author: { name: "윤스타일" },
    category: "CSS",
    createdAt: "1시간 전",
    likes: 8,
    comments: 2,
    views: 98,
    tags: ["css", "grid", "layout", "frontend"],
  },
  ...mockPosts.slice(0, 3),
]

const feedPosts: Post[] = [
  {
    id: "8",
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
    id: "9",
    title: "코드 리뷰 문화 정착시키기",
    excerpt: "팀에 코드 리뷰 문화를 정착시킨 경험을 공유합니다. 효과적인 코드 리뷰를 위한 가이드라인과 팁을 알려드립니다.",
    author: { name: "박타입" },
    category: "팀문화",
    createdAt: "1일 전",
    likes: 156,
    comments: 28,
    views: 1890,
    tags: ["code-review", "team", "collaboration"],
  },
]

export default function HomePage() {
  const [activeTab, setActiveTab] = useState("popular")

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
    </div>
  )
}
