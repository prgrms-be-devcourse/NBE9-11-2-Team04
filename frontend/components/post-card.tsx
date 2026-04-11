import Link from "next/link"
import { Heart, MessageCircle, Eye, Bookmark } from "lucide-react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"

export interface Post {
  id: string
  title: string
  excerpt: string
  author: {
    name: string
    avatar?: string
  }
  category: string
  createdAt: string
  likes: number
  comments: number
  views: number
  tags: string[]
}

interface PostCardProps {
  post: Post
}

export function PostCard({ post }: PostCardProps) {
  return (
    <article className="group rounded-lg border border-border bg-card p-6 transition-all hover:border-primary/50 hover:bg-card/80">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          {/* Category & Date */}
          <div className="mb-2 flex items-center gap-2">
            <Link
              href={`/category/${post.category.toLowerCase()}`}
              className="rounded-md bg-primary/10 px-2 py-1 text-xs font-medium text-primary transition-colors hover:bg-primary/20"
            >
              {post.category}
            </Link>
            <span className="text-xs text-muted-foreground">{post.createdAt}</span>
          </div>

          {/* Title */}
          <Link href={`/post/${post.id}`}>
            <h3 className="mb-2 text-lg font-semibold text-foreground transition-colors group-hover:text-primary">
              {post.title}
            </h3>
          </Link>

          {/* Excerpt */}
          <p className="mb-4 line-clamp-2 text-sm leading-relaxed text-muted-foreground">
            {post.excerpt}
          </p>

          {/* Tags */}
          <div className="mb-4 flex flex-wrap gap-2">
            {post.tags.map((tag) => (
              <span
                key={tag}
                className="text-xs text-muted-foreground before:content-['#']"
              >
                {tag}
              </span>
            ))}
          </div>

          {/* Author & Stats */}
          <div className="flex items-center justify-between">
            <Link href={`/user/${post.author.name}`} className="flex items-center gap-2">
              <Avatar className="h-6 w-6">
                <AvatarImage src={post.author.avatar} alt={post.author.name} />
                <AvatarFallback className="bg-secondary text-secondary-foreground text-xs">
                  {post.author.name.slice(0, 2).toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <span className="text-sm text-muted-foreground transition-colors hover:text-foreground">
                {post.author.name}
              </span>
            </Link>

            <div className="flex items-center gap-4 text-muted-foreground">
              <div className="flex items-center gap-1">
                <Heart className="h-4 w-4" />
                <span className="text-xs">{post.likes}</span>
              </div>
              <div className="flex items-center gap-1">
                <MessageCircle className="h-4 w-4" />
                <span className="text-xs">{post.comments}</span>
              </div>
              <div className="flex items-center gap-1">
                <Eye className="h-4 w-4" />
                <span className="text-xs">{post.views}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bookmark */}
        <button className="text-muted-foreground transition-colors hover:text-primary">
          <Bookmark className="h-5 w-5" />
          <span className="sr-only">북마크</span>
        </button>
      </div>
    </article>
  )
}
