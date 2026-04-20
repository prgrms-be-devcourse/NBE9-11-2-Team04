"use client"

import { useEffect, useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import Link from "next/link"
import { useParams } from "next/navigation"

type PostPageResponse = {
  content: {
    postId: number
    title: string
    content: string
    nickName: string
    categoryId: number
    viewCount: number
    likeCount: number
    commentCount: number
    createdAt: string
  }[]
}

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

// slug → categoryId 매핑
const categoryMap: Record<string, number> = {
  tech: 1,
  "job-market": 2,
  trend: 3,
  free: 4,
}

// 기존 유지
const allCategories = [
  { slug: "tech", name: "IT 기술 정보" },
  { slug: "job-market", name: "취업 시장 정보" },
  { slug: "trend", name: "개발자 트렌드" },
  { slug: "free", name: "자유 주제" },
]

const formatTimeAgo = (dateString: string) => {
  const date = new Date(dateString)
  const diff = Date.now() - date.getTime()

  const minutes = Math.floor(diff / 1000 / 60)
  if (minutes < 1) return "방금 전"
  if (minutes < 60) return `${minutes}분 전`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}시간 전`

  const days = Math.floor(hours / 24)
  return `${days}일 전`
}

export default function CategoryPage() {
  const params = useParams()
  const slug = params.slug as string

  const [posts, setPosts] = useState<Post[]>([])
  const [loading, setLoading] = useState(true)

  const categoryName =
    allCategories.find((c) => c.slug === slug)?.name || slug

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const categoryId = categoryMap[slug]

        if (!categoryId) return

        const response = await fetch(
          `${API_BASE_URL}/api/posts?categoryId=${categoryId}`
        )

        if (!response.ok) {
          throw new Error("데이터 못불러옴")
        }

        const data: PostPageResponse = await response.json()

        const mapped: Post[] = data.content.map((post) => ({
          id: String(post.postId),
          title: post.title,
          excerpt: post.content,
          author: { name: post.nickName },
          category: categoryName,
          createdAt: formatTimeAgo(post.createdAt),
          likes: post.likeCount,
          comments: post.commentCount,
          views: post.viewCount,
          tags: [],
        }))

        setPosts(mapped)
      } catch (e) {
        console.error(e)
      } finally {
        setLoading(false)
      }
    }

    fetchPosts()
  }, [slug])

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold">{categoryName}</h1>
      </div>

      {/* Navigation */}
      <div className="mb-8 flex flex-wrap gap-2">
        {allCategories.map((cat) => (
          <Link
            key={cat.slug}
            href={`/category/${cat.slug}`}
            className={`rounded-full px-4 py-2 text-sm ${
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
      {loading ? (
        <div className="text-center py-10 text-muted-foreground">
          로딩중...
        </div>
      ) : posts.length > 0 ? (
        <div className="grid gap-6">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      ) : (
        <div className="rounded-lg border p-12 text-center">
          <p className="text-lg font-semibold">
            아직 작성된 글이 없습니다
          </p>
          <Link href={`/write?category=${slug}`}>
            <button className="mt-4 px-4 py-2 bg-primary text-white rounded">
              글 쓰기
            </button>
          </Link>
        </div>
      )}
    </div>
  )
}