import { PostCard, type Post } from "@/components/post-card"
import { Clock } from "lucide-react"

// Mock latest posts data
const latestPosts: Post[] = [
  {
    id: "1",
    title: "스타트업 vs 대기업: 3년차 개발자의 솔직한 비교",
    excerpt: "스타트업과 대기업 모두 경험해본 개발자가 각각의 장단점을 솔직하게 비교합니다.",
    author: { name: "강경험" },
    category: "자유 주제",
    createdAt: "10분 전",
    likes: 5,
    comments: 1,
    views: 42,
    tags: ["커리어", "스타트업", "대기업"],
  },
  {
    id: "2",
    title: "2026년 개발자 연봉 협상 가이드",
    excerpt: "연봉 협상 시 알아야 할 팁과 실제 연봉 데이터를 기반으로 한 협상 전략을 공유합니다.",
    author: { name: "윤연봉" },
    category: "취업 시장 정보",
    createdAt: "30분 전",
    likes: 8,
    comments: 2,
    views: 98,
    tags: ["연봉", "이직", "협상"],
  },
  {
    id: "3",
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
    id: "4",
    title: "AI 코딩 어시스턴트 비교: Copilot vs Cursor vs Claude",
    excerpt: "개발 생산성을 높여주는 AI 코딩 도구들을 실제 사용 경험을 바탕으로 비교 분석합니다.",
    author: { name: "이트렌드" },
    category: "개발자 트렌드",
    createdAt: "3시간 전",
    likes: 145,
    comments: 28,
    views: 1890,
    tags: ["ai", "copilot", "개발도구"],
  },
  {
    id: "5",
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
    id: "6",
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
    id: "7",
    title: "Kubernetes 입문부터 실전까지: 완벽 가이드",
    excerpt: "컨테이너 오케스트레이션의 표준 Kubernetes를 처음부터 실전 적용까지 단계별로 설명합니다.",
    author: { name: "최데브옵스" },
    category: "IT 기술 정보",
    createdAt: "1일 전",
    likes: 256,
    comments: 42,
    views: 3240,
    tags: ["kubernetes", "devops", "인프라"],
  },
  {
    id: "8",
    title: "효과적인 코드 리뷰 문화 정착시키기",
    excerpt: "팀에 코드 리뷰 문화를 정착시킨 경험을 공유합니다. 효과적인 코드 리뷰를 위한 가이드라인과 팁을 알려드립니다.",
    author: { name: "박리뷰" },
    category: "개발자 트렌드",
    createdAt: "1일 전",
    likes: 156,
    comments: 28,
    views: 1890,
    tags: ["코드리뷰", "팀문화", "협업"],
  },
]

export default function LatestPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8 flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
          <Clock className="h-5 w-5 text-primary" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-foreground">최신글</h1>
          <p className="text-muted-foreground">방금 올라온 따끈따끈한 글들입니다</p>
        </div>
      </div>

      {/* Category Filter */}
      <div className="mb-8 flex flex-wrap gap-2">
        {["전체", "IT 기술 정보", "취업 시장 정보", "개발자 트렌드", "자유 주제"].map(
          (category, idx) => (
            <button
              key={category}
              className={`rounded-full px-4 py-2 text-sm transition-colors ${
                idx === 0
                  ? "bg-primary text-primary-foreground"
                  : "bg-secondary text-secondary-foreground hover:bg-secondary/80"
              }`}
            >
              {category}
            </button>
          )
        )}
      </div>

      {/* Posts Grid */}
      <div className="grid gap-6">
        {latestPosts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>

      {/* Load More */}
      <div className="mt-8 flex justify-center">
        <button className="rounded-lg bg-secondary px-6 py-3 text-secondary-foreground transition-colors hover:bg-secondary/80">
          더 보기
        </button>
      </div>
    </div>
  )
}
