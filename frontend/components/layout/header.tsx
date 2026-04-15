"use client"

import Link from "next/link"
import { useEffect, useState } from "react"
import { Bell, Search, User, Menu, X, PenSquare, Code2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

const categories = [
  { name: "IT 기술 정보", href: "/category/tech" },
  { name: "취업 시장 정보", href: "/category/job-market" },
  { name: "개발자 트렌드", href: "/category/trend" },
  { name: "자유 주제", href: "/category/free" },
]

const API_BASE_URL = "http://localhost:8080"

type HeaderNotificationItem = {
  notificationId: number
  isRead?: boolean
  read?: boolean
}

type HeaderNotificationListResponse = {
  notifications: HeaderNotificationItem[]
}

function isNotificationUnread(notification: HeaderNotificationItem) {
  return !(notification.isRead ?? notification.read ?? false)
}

function dispatchNotificationsUpdated() {
  if (typeof window === "undefined") {
    return
  }

  window.dispatchEvent(new CustomEvent("notifications-updated"))
}

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [hasUnreadNotifications, setHasUnreadNotifications] = useState(false)

  useEffect(() => {
    let isMounted = true

    const loadNotifications = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/notifications`, {
          cache: "no-store",
        })

        if (!response.ok) {
          if (isMounted) {
            setHasUnreadNotifications(false)
          }
          return
        }

        const data: HeaderNotificationListResponse = await response.json()

        if (!isMounted) {
          return
        }

        setHasUnreadNotifications(data.notifications.some(isNotificationUnread))
      } catch {
        if (!isMounted) {
          return
        }

        setHasUnreadNotifications(false)
      }
    }

    const handleNotificationsUpdated = () => {
      void loadNotifications()
    }

    void loadNotifications()
    window.addEventListener("notifications-updated", handleNotificationsUpdated)

    return () => {
      isMounted = false
      window.removeEventListener("notifications-updated", handleNotificationsUpdated)
    }
  }, [])

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-2">
          <Code2 className="h-7 w-7 text-primary" />
          <span className="text-xl font-bold text-foreground">DevConnect</span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden items-center gap-6 md:flex">
          {/* Categories Dropdown */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="text-muted-foreground hover:text-foreground">
                카테고리
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start" className="w-48">
              {categories.map((category) => (
                <DropdownMenuItem key={category.name} asChild>
                  <Link href={category.href}>{category.name}</Link>
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>

          <Link
            href="/popular"
            className="text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            인기글
          </Link>
          <Link
            href="/latest"
            className="text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            최신글
          </Link>
          <Link
            href="/feed"
            className="text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            피드
          </Link>
        </nav>

        {/* Actions */}
        <div className="flex items-center gap-2">
          {/* Search */}
          <Link href="/search">
            <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-foreground">
              <Search className="h-5 w-5" />
              <span className="sr-only">검색</span>
            </Button>
          </Link>

          {/* Notifications */}
          <Link href="/notifications">
            <Button variant="ghost" size="icon" className="relative text-muted-foreground hover:text-foreground">
              <Bell className="h-5 w-5" />
              {hasUnreadNotifications ? (
                <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-primary" />
              ) : null}
              <span className="sr-only">알림</span>
            </Button>
          </Link>

          {/* Write Post Button */}
          <Link href="/write" className="hidden sm:block">
            <Button className="gap-2 bg-primary text-primary-foreground hover:bg-primary/90">
              <PenSquare className="h-4 w-4" />
              글 쓰기
            </Button>
          </Link>

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-foreground">
                <User className="h-5 w-5" />
                <span className="sr-only">마이페이지</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-48">
              <DropdownMenuItem asChild>
                <Link href="/mypage">마이페이지</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/mypage/posts">내 글 보기</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/mypage/edit">프로필 수정</Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link href="/login">로그인</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/signup">회원가입</Link>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Mobile Menu Button */}
          <Button
            variant="ghost"
            size="icon"
            className="text-muted-foreground hover:text-foreground md:hidden"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            <span className="sr-only">메뉴</span>
          </Button>
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="border-t border-border bg-background md:hidden">
          <div className="mx-auto max-w-7xl px-4 py-4 sm:px-6">
            <nav className="flex flex-col gap-4">
              <Link
                href="/popular"
                className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                onClick={() => setMobileMenuOpen(false)}
              >
                인기글
              </Link>
              <Link
                href="/latest"
                className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                onClick={() => setMobileMenuOpen(false)}
              >
                최신글
              </Link>
              <Link
                href="/feed"
                className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                onClick={() => setMobileMenuOpen(false)}
              >
                피드
              </Link>
              <div className="border-t border-border pt-4">
                <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  카테고리
                </p>
                <div className="flex flex-wrap gap-2">
                  {categories.map((category) => (
                    <Link
                      key={category.name}
                      href={category.href}
                      className="rounded-md bg-secondary px-3 py-1.5 text-sm text-secondary-foreground transition-colors hover:bg-secondary/80"
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      {category.name}
                    </Link>
                  ))}
                </div>
              </div>
              <Link href="/write" onClick={() => setMobileMenuOpen(false)}>
                <Button className="w-full gap-2 bg-primary text-primary-foreground hover:bg-primary/90">
                  <PenSquare className="h-4 w-4" />
                  글 쓰기
                </Button>
              </Link>
            </nav>
          </div>
        </div>
      )}
    </header>
  )
}
