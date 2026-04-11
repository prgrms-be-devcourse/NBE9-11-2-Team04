import { PostCard, type Post } from "@/components/post-card"
import Link from "next/link"

// Mock data for categories
const categoryData: Record<string, { name: string; description: string; posts: Post[] }> = {
  tech: {
    name: "IT 기술 정보",
    description: "프로그래밍 언어, 프레임워크, 개발 도구 등 IT 기술 관련 정보를 공유합니다.",
    posts: [
      {
        id: "tech-1",
        title: "2026년 프론트엔드 개발자 로드맵: 꼭 알아야 할 기술 스택",
        excerpt: "React, Next.js, TypeScript를 중심으로 2026년 프론트엔드 개발자가 반드시 알아야 할 기술들을 정리했습니다.",
        author: { name: "김개발" },
        category: "IT 기술 정보",
        createdAt: "3시간 전",
        likes: 145,
        comments: 28,
        views: 1890,
        tags: ["frontend", "react", "개발로드맵"],
      },
      {
        id: "tech-2",
        title: "Kubernetes 입문부터 실전까지: 완벽 가이드",
        excerpt: "컨테이너 오케스트레이션의 표준 Kubernetes를 처음부터 실전 적용까지 단계별로 설명합니다.",
        author: { name: "최데브옵스" },
        category: "IT 기술 정보",
        createdAt: "1일 전",
        likes: 234,
        comments: 45,
        views: 2890,
        tags: ["kubernetes", "devops", "인프라"],
      },
      {
        id: "tech-3",
        title: "Spring Boot 3.0 마이그레이션 실전 가이드",
        excerpt: "Spring Boot 2.x에서 3.0으로 마이그레이션하면서 겪은 이슈와 해결 방법을 공유합니다.",
        author: { name: "박스프링" },
        category: "IT 기술 정보",
        createdAt: "2일 전",
        likes: 189,
        comments: 32,
        views: 2340,
        tags: ["spring", "java", "backend"],
      },
    ],
  },
  "job-market": {
    name: "취업 시장 정보",
    description: "개발자 채용 동향, 연봉 정보, 이직 팁 등 취업 시장 관련 정보를 나눕니다.",
    posts: [
      {
        id: "job-1",
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
        id: "job-2",
        title: "2026년 개발자 연봉 협상 가이드",
        excerpt: "연봉 협상 시 알아야 할 팁과 실제 연봉 데이터를 기반으로 한 협상 전략을 공유합니다.",
        author: { name: "윤연봉" },
        category: "취업 시장 정보",
        createdAt: "1일 전",
        likes: 312,
        comments: 67,
        views: 4580,
        tags: ["연봉", "이직", "협상"],
      },
      {
        id: "job-3",
        title: "개발자 포트폴리오 작성법: 면접관이 보는 포인트",
        excerpt: "실제 면접관 경험을 바탕으로 좋은 포트폴리오의 조건과 작성 팁을 알려드립니다.",
        author: { name: "김면접" },
        category: "취업 시장 정보",
        createdAt: "3일 전",
        likes: 256,
        comments: 48,
        views: 3120,
        tags: ["포트폴리오", "면접", "취업"],
      },
    ],
  },
  trend: {
    name: "개발자 트렌드",
    description: "최신 개발 트렌드, 새로운 기술, 개발 문화 등 트렌드 정보를 공유합니다.",
    posts: [
      {
        id: "trend-1",
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
        id: "trend-2",
        title: "효과적인 코드 리뷰 문화 정착시키기",
        excerpt: "팀에 코드 리뷰 문화를 정착시킨 경험을 공유합니다. 효과적인 코드 리뷰를 위한 가이드라인과 팁을 알려드립니다.",
        author: { name: "박리뷰" },
        category: "개발자 트렌드",
        createdAt: "2일 전",
        likes: 156,
        comments: 28,
        views: 1890,
        tags: ["코드리뷰", "팀문화", "협업"],
      },
    ],
  },
  free: {
    name: "자유 주제",
    description: "개발과 관련된 자유로운 주제로 소통하는 공간입니다.",
    posts: [
      {
        id: "free-1",
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
      {
        id: "free-2",
        title: "스타트업 vs 대기업: 3년차 개발자의 솔직한 비교",
        excerpt: "스타트업과 대기업 모두 경험해본 개발자가 각각의 장단점을 솔직하게 비교합니다.",
        author: { name: "강경험" },
        category: "자유 주제",
        createdAt: "4일 전",
        likes: 234,
        comments: 89,
        views: 3450,
        tags: ["커리어", "스타트업", "대기업"],
      },
      {
        id: "free-3",
        title: "개발자의 번아웃 예방: 나만의 루틴 만들기",
        excerpt: "10년차 개발자가 알려주는 번아웃 예방법. 지속 가능한 개발자 생활을 위한 팁들을 공유합니다.",
        author: { name: "김개발" },
        category: "자유 주제",
        createdAt: "5일 전",
        likes: 189,
        comments: 45,
        views: 2890,
        tags: ["번아웃", "루틴", "개발자생활"],
      },
    ],
  },
}

const allCategories = [
  { slug: "tech", name: "IT 기술 정보" },
  { slug: "job-market", name: "취업 시장 정보" },
  { slug: "trend", name: "개발자 트렌드" },
  { slug: "free", name: "자유 주제" },
]

interface CategoryPageProps {
  params: Promise<{ slug: string }>
}

export default async function CategoryPage({ params }: CategoryPageProps) {
  const { slug } = await params
  const category = categoryData[slug] || {
    name: slug.charAt(0).toUpperCase() + slug.slice(1),
    description: "",
    posts: [],
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Category Header */}
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-foreground">{category.name}</h1>
        {category.description && (
          <p className="text-muted-foreground">{category.description}</p>
        )}
      </div>

      {/* Category Navigation */}
      <div className="mb-8 flex flex-wrap gap-2">
        {allCategories.map((cat) => (
          <Link
            key={cat.slug}
            href={`/category/${cat.slug}`}
            className={`rounded-full px-4 py-2 text-sm transition-colors ${
              cat.slug === slug
                ? "bg-primary text-primary-foreground"
                : "bg-secondary text-secondary-foreground hover:bg-secondary/80"
            }`}
          >
            {cat.name}
          </Link>
        ))}
      </div>

      {/* Posts */}
      {category.posts.length > 0 ? (
        <div className="grid gap-6">
          {category.posts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      ) : (
        <div className="rounded-lg border border-border bg-card p-12 text-center">
          <p className="text-lg font-semibold text-foreground">
            아직 작성된 글이 없습니다
          </p>
          <p className="mt-2 text-sm text-muted-foreground">
            첫 번째 글을 작성해보세요!
          </p>
          <Link href="/write">
            <button className="mt-4 rounded-lg bg-primary px-4 py-2 text-primary-foreground hover:bg-primary/90">
              글 쓰기
            </button>
          </Link>
        </div>
      )}
    </div>
  )
}

export function generateStaticParams() {
  return allCategories.map((cat) => ({
    slug: cat.slug,
  }))
}
