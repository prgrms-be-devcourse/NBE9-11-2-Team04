"use client"

import { useEffect, useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { TrendingUp, Clock, Users } from "lucide-react"


const feedPosts: Post[] = []

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

  const getPostsForTab = () => {
    if (activeTab === "feed") {
      return feedPosts
    }
    return posts
  }

  useEffect(() => {
    const fetchPosts = async () => {
      setLoading(true)

      try {
        let url = ""

        if (activeTab === "popular") {
          url = "http://localhost:8080/api/posts?sort=LIKES"
        } else if (activeTab === "latest") {
          url = "http://localhost:8080/api/posts?sort=LATEST"
        } else {
          return
        }

        const res = await fetch(url)
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
            {loading ? (
                <div className="text-center py-10 text-muted-foreground">
                  로딩중...
                </div>
            ) : (
                <div className="grid gap-6">
                  {getPostsForTab().map((post) => (
                      <PostCard key={post.id} post={post} />
                  ))}
                </div>
            )}
          </TabsContent>

          <TabsContent value="latest" className="mt-0">
            {loading ? (
                <div className="text-center py-10 text-muted-foreground">
                  로딩중...
                </div>
            ) : (
                <div className="grid gap-6">
                  {getPostsForTab().map((post) => (
                      <PostCard key={post.id} post={post} />
                  ))}
                </div>
            )}
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