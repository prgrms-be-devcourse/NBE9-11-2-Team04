"use client"

import { useState, useEffect, useCallback } from "react"
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

const categories = [
  "전체",
  "IT 기술 정보",
  "취업 시장 정보",
  "개발자 트렌드",
  "자유 주제",
]

const sortOptions = [
  { value: "relevance", label: "관련도순" },
  { value: "latest", label: "최신순" },
  { value: "popular", label: "인기순" },
  { value: "comments", label: "댓글순" },
]

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
  const [sortBy, setSortBy] = useState("relevance")
  const [selectedCategory, setSelectedCategory] = useState("전체")
  const [recentSearches, setRecentSearches] = useState<string[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setRecentSearches(getSearchHistory())
  }, [])

  const handleSearch = useCallback(
    async (searchQuery: string, signal?: AbortSignal) => {
      const trimmed = searchQuery.trim()

      if (!trimmed) {
        setResults([])
        setError(null)
        setIsSearching(false)
        return
      }

      try {
        setIsSearching(true)
        setError(null)

        const params = new URLSearchParams({
          q: trimmed,
          sortBy,
        })

        if (selectedCategory !== "전체") {
          params.set("category", selectedCategory)
        }

        const response = await fetch(`/api/posts/search?${params.toString()}`, {
          method: "GET",
          signal,
          cache: "no-store",
        })

        if (!response.ok) {
          throw new Error("검색 요청에 실패했습니다.")
        }

        const data = await response.json()

        setResults(Array.isArray(data.posts) ? data.posts : [])

        const nextHistory = saveSearchHistory(trimmed)
        setRecentSearches(nextHistory)
      } catch (err) {
        if (err instanceof DOMException && err.name === "AbortError") {
          return
        }

        console.error(err)
        setResults([])
        setError("검색 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.")
      } finally {
        if (!signal?.aborted) {
          setIsSearching(false)
        }
      }
    },
    [sortBy, selectedCategory]
  )

  useEffect(() => {
    const trimmed = query.trim()

    if (!trimmed) {
      setResults([])
      setError(null)
      setIsSearching(false)
      return
    }

    const controller = new AbortController()

    const debounce = setTimeout(() => {
      handleSearch(trimmed, controller.signal)
    }, 300)

    return () => {
      clearTimeout(debounce)
      controller.abort()
    }
  }, [query, sortBy, selectedCategory, handleSearch])

  const clearSearch = () => {
    setQuery("")
    setResults([])
    setError(null)
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
                type="button"
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
                        type="button"
                        variant={selectedCategory === category ? "default" : "outline"}
                        size="sm"
                        onClick={() => setSelectedCategory(category)}
                        className={
                          selectedCategory === category
                            ? "bg-primary text-primary-foreground"
                            : ""
                        }
                      >
                        {category}
                      </Button>
                    ))}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>기간</Label>
                  <div className="space-y-2">
                    {["전체 기간", "오늘", "이번 주", "이번 달", "올해"].map(
                      (period) => (
                        <div key={period} className="flex items-center gap-2">
                          <Checkbox id={period} />
                          <Label htmlFor={period} className="font-normal">
                            {period}
                          </Label>
                        </div>
                      )
                    )}
                  </div>
                </div>
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>

      {!query && recentSearches.length > 0 && (
        <div className="mb-8">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-muted-foreground">
              최근 검색어
            </h2>
            <button
              type="button"
              onClick={clearAllRecentSearches}
              className="text-sm text-muted-foreground hover:text-foreground"
            >
              전체 삭제
            </button>
          </div>

          <div className="flex flex-wrap gap-2">
            {recentSearches.map((search) => (
              <div
                key={search}
                className="flex items-center gap-1 rounded-full border border-border bg-secondary px-3 py-1.5"
              >
                <button
                  type="button"
                  onClick={() => handleRecentSearchClick(search)}
                  className="text-sm text-foreground hover:text-primary"
                >
                  {search}
                </button>
                <button
                  type="button"
                  onClick={() => removeRecentSearch(search)}
                  className="text-muted-foreground hover:text-foreground"
                >
                  <X className="h-3 w-3" />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {query && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <p className="text-sm text-muted-foreground">
              {isSearching ? "검색 중..." : `"${query}" 검색 결과 ${results.length}건`}
            </p>
            <Select value={sortBy} onValueChange={setSortBy}>
              <SelectTrigger className="w-32">
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

          {error ? (
            <div className="rounded-lg border border-border bg-card p-12 text-center">
              <h3 className="mb-2 text-lg font-semibold text-foreground">
                오류가 발생했습니다
              </h3>
              <p className="text-sm text-muted-foreground">{error}</p>
            </div>
          ) : isSearching ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="h-40 animate-pulse rounded-lg bg-secondary"
                />
              ))}
            </div>
          ) : results.length > 0 ? (
            <div className="grid gap-6">
              {results.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>
          ) : (
            <div className="rounded-lg border border-border bg-card p-12 text-center">
              <Search className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold text-foreground">
                검색 결과가 없습니다
              </h3>
              <p className="text-sm text-muted-foreground">
                다른 키워드로 검색해보세요
              </p>
            </div>
          )}
        </div>
      )}

      {!query && (
        <div>
          <h2 className="mb-4 text-sm font-semibold text-muted-foreground">
            인기 태그
          </h2>
          <div className="flex flex-wrap gap-2">
            {[
              "취업",
              "신입채용",
              "이직",
              "연봉협상",
              "코딩테스트",
              "포트폴리오",
              "ai",
              "frontend",
              "backend",
              "개발자생활",
            ].map((tag) => (
              <button
                key={tag}
                type="button"
                onClick={() => handleRecentSearchClick(tag)}
                className="rounded-full border border-border bg-secondary px-3 py-1.5 text-sm text-foreground transition-colors hover:bg-primary hover:text-primary-foreground"
              >
                #{tag}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}