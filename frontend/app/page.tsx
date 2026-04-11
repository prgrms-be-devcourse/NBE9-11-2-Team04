"use client"

import { useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { TrendingUp, Clock, Users } from "lucide-react"

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
