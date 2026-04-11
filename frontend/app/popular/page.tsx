import { PostCard, type Post } from "@/components/post-card"
import { TrendingUp } from "lucide-react"

// Mock popular posts data
const popularPosts: Post[] = [
  {
    id: "1",
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
  {
    id: "2",
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
    id: "3",
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
    id: "4",
    title: "클로저(Closure) 완벽 이해하기",
    excerpt: "JavaScript의 핵심 개념인 클로저를 실제 예제와 함께 깊이있게 살펴봅니다.",
    author: { name: "박클로저" },
    category: "JavaScript",
    createdAt: "1일 전",
    likes: 234,
    comments: 45,
    views: 2890,
    tags: ["javascript", "closure", "fundamentals"],
  },
  {
    id: "5",
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
    id: "6",
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
  {
    id: "7",
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
    id: "8",
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
]

export default function PopularPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8 flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
          <TrendingUp className="h-5 w-5 text-primary" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-foreground">인기글</h1>
          <p className="text-muted-foreground">가장 많은 관심을 받은 글들입니다</p>
        </div>
      </div>

      {/* Period Selector */}
      <div className="mb-8 flex gap-2">
        {["오늘", "이번 주", "이번 달", "전체"].map((period, idx) => (
          <button
            key={period}
            className={`rounded-full px-4 py-2 text-sm transition-colors ${
              idx === 1
                ? "bg-primary text-primary-foreground"
                : "bg-secondary text-secondary-foreground hover:bg-secondary/80"
            }`}
          >
            {period}
          </button>
        ))}
      </div>

      {/* Posts Grid */}
      <div className="grid gap-6">
        {popularPosts.map((post, index) => (
          <div key={post.id} className="relative">
            {/* Rank Badge */}
            <div className="absolute -left-2 -top-2 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-bold text-primary-foreground">
              {index + 1}
            </div>
            <PostCard post={post} />
          </div>
        ))}
      </div>
    </div>
  )
}
