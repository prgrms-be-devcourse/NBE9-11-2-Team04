import { PostCard, type Post } from "@/components/post-card"
import { Clock } from "lucide-react"

// Mock latest posts data
const latestPosts: Post[] = [
  {
    id: "1",
    title: "Zustand vs Jotai: 상태관리 라이브러리 비교",
    excerpt: "가벼운 상태관리 라이브러리로 주목받는 Zustand와 Jotai를 비교 분석합니다. 각각의 장단점과 적합한 사용 사례를 알아봅니다.",
    author: { name: "강상태" },
    category: "React",
    createdAt: "10분 전",
    likes: 5,
    comments: 1,
    views: 42,
    tags: ["react", "state-management", "zustand", "jotai"],
  },
  {
    id: "2",
    title: "CSS Grid 마스터하기: 실전 레이아웃 예제",
    excerpt: "CSS Grid를 활용한 다양한 실전 레이아웃 예제를 살펴봅니다. 복잡한 레이아웃도 쉽게 구현할 수 있습니다.",
    author: { name: "윤스타일" },
    category: "CSS",
    createdAt: "30분 전",
    likes: 8,
    comments: 2,
    views: 98,
    tags: ["css", "grid", "layout", "frontend"],
  },
  {
    id: "3",
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
    id: "4",
    title: "JavaScript ES2024 새로운 기능 미리보기",
    excerpt: "ES2024에서 추가될 예정인 새로운 기능들을 살펴봅니다. Array grouping, Promise.withResolvers 등 유용한 기능들이 많습니다.",
    author: { name: "김자스" },
    category: "JavaScript",
    createdAt: "3시간 전",
    likes: 145,
    comments: 28,
    views: 1890,
    tags: ["javascript", "es2024", "ecmascript"],
  },
  {
    id: "5",
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
    id: "6",
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
    id: "7",
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
    id: "8",
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
        {["전체", "JavaScript", "TypeScript", "React", "Next.js", "Python", "DevOps", "커리어"].map(
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
