"use client"

import { useEffect, useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { TrendingUp, Clock, Users } from "lucide-react"

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

export default function HomePage() {
  const [activeTab, setActiveTab] = useState("popular")
  const [posts, setPosts] = useState<Post[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchPosts = async () => {
      setLoading(true)

      try {
        let url = ""

        if (activeTab === "popular") {
          url = `${API_BASE_URL}/api/posts?sort=LIKES`
        } else if (activeTab === "latest") {
          url = `${API_BASE_URL}/api/posts?sort=LATEST`
        } else {
          // feed는 아직 API 없으니까 빈 배열
          setPosts([])
          return
        }

        const res = await fetch(url)

        if (!res.ok) {
          throw new Error("게시글 불러오기 실패")
        }

        const data: PostPageResponse = await res.json()

        const mapped: Post[] = data.content.map((post) => ({
          id: String(post.postId),
          title: post.title,
          excerpt: post.content,
          author: {
            name: post.nickName,
          },
          category: String(post.categoryId),
          createdAt: formatTimeAgo(post.createdAt),
          likes: post.likeCount,
          comments: post.commentCount,
          views: post.viewCount,
          tags: [],
        }))

        setPosts(mapped)
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    fetchPosts()
  }, [activeTab])

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Hero */}
      <section className="mb-12 text-center">
        <h1 className="mb-4 text-4xl font-bold sm:text-5xl">
          개발자들의 <span className="text-primary">지식 허브</span>
        </h1>
        <p className="mx-auto max-w-2xl text-lg text-muted-foreground">
          최신 기술 트렌드, 실무 경험을 나눠보세요.
        </p>
      </section>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-8 grid w-full max-w-md mx-auto grid-cols-3">
          <TabsTrigger value="popular">
            <TrendingUp className="h-4 w-4" /> 인기글
          </TabsTrigger>
          <TabsTrigger value="latest">
            <Clock className="h-4 w-4" /> 최신글
          </TabsTrigger>
          <TabsTrigger value="feed">
            <Users className="h-4 w-4" /> 피드
          </TabsTrigger>
        </TabsList>

        {/* 인기글 */}
        <TabsContent value="popular">
          {loading ? (
            <div className="text-center py-10">로딩중...</div>
          ) : (
            <div className="grid gap-6">
              {posts.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>
          )}
        </TabsContent>

        {/* 최신글 */}
        <TabsContent value="latest">
          {loading ? (
            <div className="text-center py-10">로딩중...</div>
          ) : (
            <div className="grid gap-6">
              {posts.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>
          )}
        </TabsContent>

        {/* 피드 */}
        <TabsContent value="feed">
          <div className="text-center py-10 text-muted-foreground">
            아직 피드 기능은 구현되지 않았습니다
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}