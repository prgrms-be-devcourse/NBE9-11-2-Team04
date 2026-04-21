import Link from "next/link"
import { MessageCircle, Eye } from "lucide-react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import InteractionButtons from "@/components/interaction-buttons"

export interface Post {
  id: string
  title: string
  excerpt: string
  author: {
    name: string
    avatar?: string
    userId?: number
  }
  category: string
  createdAt: string
  likes: number
  comments: number
  views: number
  tags: string[]
  liked?: boolean
  bookmarked?: boolean
}

interface PostCardProps {
  post: Post
  onBookmarkToggle?: (postId: number, nextBookmarked: boolean) => void
}

export function PostCard({ post, onBookmarkToggle }: PostCardProps) {
  const authorProfileHref = post.author.userId
    ? `/users/${post.author.userId}`
    : undefined

  return (
    <article className="group rounded-lg border border-border bg-card p-6 transition-all hover:border-primary/50 hover:bg-card/80">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="mb-2 flex items-center gap-2">
            <Link
              href={`/category/${post.category.toLowerCase()}`}
              className="rounded-md bg-primary/10 px-2 py-1 text-xs font-medium text-primary transition-colors hover:bg-primary/20"
            >
              {post.category}
            </Link>
            <span className="text-xs text-muted-foreground">
              {post.createdAt}
            </span>
          </div>

          <Link href={`/posts/${post.id}`}>
            <h3 className="mb-2 text-lg font-semibold text-foreground transition-colors group-hover:text-primary">
              {post.title}
            </h3>
          </Link>

          <p className="mb-4 line-clamp-2 text-sm leading-relaxed text-muted-foreground">
            {post.excerpt}
          </p>

          {post.tags.length > 0 && (
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
          )}

          <div className="flex items-center justify-between">
            {authorProfileHref ? (
              <Link href={authorProfileHref} className="flex items-center gap-2">
                <Avatar className="h-6 w-6">
                  <AvatarImage src={post.author.avatar} alt={post.author.name} />
                  <AvatarFallback className="bg-secondary text-xs text-secondary-foreground">
                    {post.author.name.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <span className="text-sm text-muted-foreground transition-colors hover:text-foreground">
                  {post.author.name}
                </span>
              </Link>
            ) : (
              <div className="flex items-center gap-2">
                <Avatar className="h-6 w-6">
                  <AvatarImage src={post.author.avatar} alt={post.author.name} />
                  <AvatarFallback className="bg-secondary text-xs text-secondary-foreground">
                    {post.author.name.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <span className="text-sm text-muted-foreground">
                  {post.author.name}
                </span>
              </div>
            )}
            <div className="flex items-center gap-4 text-muted-foreground">
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

        <InteractionButtons
          key={`${post.id}-${post.liked}-${post.bookmarked}-${post.likes}`}
          postId={Number(post.id)}
          initialLiked={post.liked ?? false}
          initialBookmarked={post.bookmarked ?? false}
          initialLikeCount={post.likes ?? 0}
          onBookmarkToggle={(nextBookmarked) =>
            onBookmarkToggle?.(Number(post.id), nextBookmarked)
          }
        />
      </div>
    </article>
  )
}