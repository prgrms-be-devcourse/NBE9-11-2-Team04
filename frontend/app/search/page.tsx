"use client"

import { useState, useEffect } from "react"
import { Search, X, SlidersHorizontal } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { PostCard, type Post } from "@/components/post-card"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"

const SEARCH_HISTORY_KEY = "searchHistory"
const MAX_RECENT_SEARCHES = 5

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

const categories = [
  "전체",
  "IT 기술 정보",
  "취업 시장 정보",
  "개발자 트렌드",
  "자유 주제",
]

const categoryMap: Record<string, number> = {
  "IT 기술 정보": 1,
  "취업 시장 정보": 2,
  "개발자 트렌드": 3,
  "자유 주제": 4,
}

const sortOptions = [
  //{ value: "relevance", label: "관련도순" },
  { value: "latest", label: "최신순" },
  { value: "popular", label: "인기순" },
  { value: "comments", label: "댓글순" },
]

const sortMap: Record<string, string> = {
  //relevance: "RELEVANCE",
  latest: "LATEST",
  popular: "LIKES",
  comments: "COMMENT",
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

function getSearchHistory(): string[] {
  if (typeof window === "undefined") return []
  const raw = localStorage.getItem(SEARCH_HISTORY_KEY)
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    return parsed.filter(
      (item): item is string => typeof item === "string" && item.trim().length > 0
    )
  } catch {
    return []
  }
}

function saveSearchHistory(keyword: string): string[] {
  if (typeof window === "undefined") return []

  const trimmed = keyword.trim()
  if (!trimmed) return getSearchHistory()

  const prev = getSearchHistory()
  const next = [
    trimmed,
    ...prev.filter((item) => item.toLowerCase() !== trimmed.toLowerCase()),
  ].slice(0, MAX_RECENT_SEARCHES)

  localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(next))
  return next
}

function removeSearchHistory(keyword: string): string[] {
  if (typeof window === "undefined") return []

  const next = getSearchHistory().filter(
    (item) => item.toLowerCase() !== keyword.trim().toLowerCase()
  )

  localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(next))
  return next
}

function clearSearchHistory() {
  if (typeof window === "undefined") return
  localStorage.removeItem(SEARCH_HISTORY_KEY)
}

export default function SearchPage() {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<Post[]>([])
  const [isSearching, setIsSearching] = useState(false)
  const [sortBy, setSortBy] = useState("latest")
  const [selectedCategory, setSelectedCategory] = useState("전체")
  const [searchType, setSearchType] = useState("TITLE_OR_CONTENT")
  const [recentSearches, setRecentSearches] = useState<string[]>([])

  useEffect(() => {
    setRecentSearches(getSearchHistory())
  }, [])

  const handleSearch = async (searchQuery: string) => {
    const trimmed = searchQuery.trim()

    if (!trimmed) {
      setResults([])
      return
    }

    setIsSearching(true)

    try {
      const categoryId =
        selectedCategory === "전체"
          ? null
          : categoryMap[selectedCategory]

      const queryParams = new URLSearchParams({
        keyword: trimmed,
        searchType: searchType,
        sort: sortMap[sortBy],
        ...(categoryId ? { categoryId: String(categoryId) } : {}),
      })

      const res = await fetch(
        `${API_BASE_URL}/api/posts?${queryParams}`
      )

      if (!res.ok) throw new Error("검색 실패")

      const data = await res.json()

      const mapped: Post[] = data.content.map((post: any) => ({
        id: String(post.postId),
        title: post.title,
        excerpt: post.content,
        author: { name: post.nickName },
        category:
          categories.find(
            (c) => categoryMap[c] === post.categoryId
          ) || "",
          createdAt: formatTimeAgo(post.createdAt),
        likes: post.likeCount,
        comments: post.commentCount,
        views: post.viewCount,
        tags: [],
      }))

      setResults(mapped)
    } catch (e) {
      console.error(e)
      setResults([])
    } finally {
      setIsSearching(false)
    }

    const nextHistory = saveSearchHistory(trimmed)
    setRecentSearches(nextHistory)
  }

  useEffect(() => {
    const debounce = setTimeout(() => {
      if (query.trim()) {
        handleSearch(query)
      }
    }, 300)

    return () => clearTimeout(debounce)
  }, [query])

  useEffect(() => {
    const debounce = setTimeout(() => {
      if (query.trim()) {
        handleSearch(query)
      }
    }, 300)
  
    return () => clearTimeout(debounce)
  }, [query, selectedCategory, sortBy, searchType])

  const clearSearch = () => {
    setQuery("")
    setResults([])
  }

  const removeRecentSearch = (search: string) => {
    const next = removeSearchHistory(search)
    setRecentSearches(next)
  }

  const clearAllRecentSearches = () => {
    clearSearchHistory()
    setRecentSearches([])
  }

  const handleRecentSearchClick = (search: string) => {
    setQuery(search)
    const next = saveSearchHistory(search)
    setRecentSearches(next)
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="mb-6 text-3xl font-bold text-foreground">글 검색</h1>

        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="text"
              placeholder="검색어를 입력하세요..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="bg-secondary pl-10 pr-10"
            />
            {query && (
              <button
                onClick={clearSearch}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            )}
          </div>

          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" className="gap-2">
                <SlidersHorizontal className="h-4 w-4" />
                <span className="hidden sm:inline">필터</span>
              </Button>
            </SheetTrigger>
            <SheetContent>
              <SheetHeader>
                <SheetTitle>검색 필터</SheetTitle>
                <SheetDescription>
                  원하는 조건으로 검색 결과를 필터링하세요
                </SheetDescription>
              </SheetHeader>

              <div className="mt-6 space-y-6">

                {/* 🔥 추가된 검색 타입 */}
                <div className="space-y-2">
                  <Label>검색 범위</Label>
                  <Select value={searchType} onValueChange={setSearchType}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="TITLE">제목</SelectItem>
                      <SelectItem value="CONTENT">내용</SelectItem>
                      <SelectItem value="TITLE_OR_CONTENT">제목 + 내용</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>정렬</Label>
                  <Select value={sortBy} onValueChange={setSortBy}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {sortOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>카테고리</Label>
                  <div className="flex flex-wrap gap-2">
                    {categories.map((category) => (
                      <Button
                        key={category}
                        variant={selectedCategory === category ? "default" : "outline"}
                        size="sm"
                        onClick={() => setSelectedCategory(category)}
                      >
                        {category}
                      </Button>
                    ))}
                  </div>
                </div>

              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>

      {query && (
        <div className="grid gap-6">
          {results.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      )}
    </div>
  )
}