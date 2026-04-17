"use client"

import { useEffect, useState } from "react"
import { PostCard, type Post } from "@/components/post-card"
import { Clock } from "lucide-react"

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

export default function LatestPage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/posts?sort=LATEST")
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
  }, [])

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8 flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
          <Clock className="h-5 w-5 text-primary" />
        </div>
        <div>
          <h1 className="text-3xl font-bold">최신글</h1>
          <p className="text-muted-foreground">
            방금 올라온 따끈따끈한 글들입니다
          </p>
        </div>
      </div>

      {/* Posts */}
      {loading ? (
        <div className="text-center py-10 text-muted-foreground">
          로딩중...
        </div>
      ) : (
        <div className="grid gap-6">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      )}
    </div>
  )
}