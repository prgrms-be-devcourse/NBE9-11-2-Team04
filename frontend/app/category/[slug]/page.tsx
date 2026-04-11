import { PostCard, type Post } from "@/components/post-card"
import Link from "next/link"

// Mock data for categories
const categoryData: Record<string, { name: string; description: string; posts: Post[] }> = {
  javascript: {
    name: "JavaScript",
    description: "웹 개발의 핵심 언어인 JavaScript 관련 글들을 모아봅니다.",
    posts: [
      {
        id: "js-1",
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
        id: "js-2",
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
    ],
  },
  typescript: {
    name: "TypeScript",
    description: "타입 안전한 JavaScript 개발을 위한 TypeScript 관련 글들입니다.",
    posts: [
      {
        id: "ts-1",
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
    ],
  },
  react: {
    name: "React",
    description: "Meta에서 개발한 UI 라이브러리 React 관련 글들입니다.",
    posts: [
      {
        id: "react-1",
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
    ],
  },
  nextjs: {
    name: "Next.js",
    description: "Vercel에서 개발한 React 프레임워크 Next.js 관련 글들입니다.",
    posts: [
      {
        id: "next-1",
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
    ],
  },
  nodejs: {
    name: "Node.js",
    description: "서버사이드 JavaScript 런타임 Node.js 관련 글들입니다.",
    posts: [],
  },
  python: {
    name: "Python",
    description: "범용 프로그래밍 언어 Python 관련 글들입니다.",
    posts: [
      {
        id: "py-1",
        title: "Python으로 만드는 AI 챗봇: GPT API 활용법",
        excerpt: "OpenAI GPT API를 활용하여 나만의 AI 챗봇을 만드는 방법을 소개합니다.",
        author: { name: "정파이썬" },
        category: "Python",
        createdAt: "3일 전",
        likes: 312,
        comments: 58,
        views: 4580,
        tags: ["python", "ai", "gpt", "chatbot"],
      },
    ],
  },
  devops: {
    name: "DevOps",
    description: "개발과 운영을 통합하는 DevOps 관련 글들입니다.",
    posts: [
      {
        id: "devops-1",
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
    ],
  },
  "ai-ml": {
    name: "AI/ML",
    description: "인공지능과 머신러닝 관련 글들입니다.",
    posts: [],
  },
}

const allCategories = [
  { slug: "javascript", name: "JavaScript" },
  { slug: "typescript", name: "TypeScript" },
  { slug: "react", name: "React" },
  { slug: "nextjs", name: "Next.js" },
  { slug: "nodejs", name: "Node.js" },
  { slug: "python", name: "Python" },
  { slug: "devops", name: "DevOps" },
  { slug: "ai-ml", name: "AI/ML" },
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
