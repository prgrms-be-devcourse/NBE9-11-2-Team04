"use client"

import { useState } from "react"
import Link from "next/link"
import { Bell, MessageCircle, Heart, UserPlus, Check, Trash2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

interface Notification {
  id: string
  type: "comment" | "reply" | "like" | "follow"
  user: {
    name: string
    avatar?: string
  }
  content: string
  postTitle?: string
  postId?: string
  createdAt: string
  isRead: boolean
}

// Mock notifications
const mockNotifications: Notification[] = [
  {
    id: "1",
    type: "comment",
    user: { name: "이리액트" },
    content: "좋은 글 감사합니다! 질문이 있는데요...",
    postTitle: "Next.js 16에서 달라진 점들: 실무에서 바로 적용하기",
    postId: "1",
    createdAt: "10분 전",
    isRead: false,
  },
  {
    id: "2",
    type: "reply",
    user: { name: "박타입" },
    content: "네, 말씀하신 부분이 맞습니다. 추가로...",
    postTitle: "TypeScript 5.0 새로운 기능 총정리",
    postId: "2",
    createdAt: "30분 전",
    isRead: false,
  },
  {
    id: "3",
    type: "like",
    user: { name: "정파이썬" },
    content: "님이 회원님의 글을 좋아합니다",
    postTitle: "개발자의 번아웃 예방: 나만의 루틴 만들기",
    postId: "3",
    createdAt: "1시간 전",
    isRead: false,
  },
  {
    id: "4",
    type: "follow",
    user: { name: "최데브옵스" },
    content: "님이 회원님을 팔로우하기 시작했습니다",
    createdAt: "2시간 전",
    isRead: true,
  },
  {
    id: "5",
    type: "comment",
    user: { name: "강상태" },
    content: "이 부분 정말 도움이 됐어요!",
    postTitle: "React Server Components 완벽 가이드",
    postId: "4",
    createdAt: "3시간 전",
    isRead: true,
  },
  {
    id: "6",
    type: "like",
    user: { name: "윤스타일" },
    content: "님이 회원님의 글을 좋아합니다",
    postTitle: "CSS Grid 마스터하기: 실전 레이아웃 예제",
    postId: "5",
    createdAt: "5시간 전",
    isRead: true,
  },
]

function getNotificationIcon(type: string) {
  switch (type) {
    case "comment":
    case "reply":
      return <MessageCircle className="h-4 w-4" />
    case "like":
      return <Heart className="h-4 w-4" />
    case "follow":
      return <UserPlus className="h-4 w-4" />
    default:
      return <Bell className="h-4 w-4" />
  }
}

function getNotificationColor(type: string) {
  switch (type) {
    case "comment":
    case "reply":
      return "bg-blue-500/20 text-blue-400"
    case "like":
      return "bg-red-500/20 text-red-400"
    case "follow":
      return "bg-green-500/20 text-green-400"
    default:
      return "bg-primary/20 text-primary"
  }
}

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState(mockNotifications)
  const [activeTab, setActiveTab] = useState("all")

  const unreadCount = notifications.filter((n) => !n.isRead).length
  const commentNotifications = notifications.filter(
    (n) => n.type === "comment" || n.type === "reply"
  )
  const likeNotifications = notifications.filter((n) => n.type === "like")

  const markAllAsRead = () => {
    setNotifications((prev) =>
      prev.map((n) => ({ ...n, isRead: true }))
    )
  }

  const deleteNotification = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id))
  }

  const getFilteredNotifications = () => {
    switch (activeTab) {
      case "comments":
        return commentNotifications
      case "likes":
        return likeNotifications
      default:
        return notifications
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
            <Bell className="h-5 w-5 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-foreground">알림</h1>
            {unreadCount > 0 && (
              <p className="text-sm text-muted-foreground">
                읽지 않은 알림 {unreadCount}개
              </p>
            )}
          </div>
        </div>
        {unreadCount > 0 && (
          <Button variant="outline" onClick={markAllAsRead} className="gap-2">
            <Check className="h-4 w-4" />
            모두 읽음
          </Button>
        )}
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6 w-full justify-start bg-secondary">
          <TabsTrigger
            value="all"
            className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            전체
          </TabsTrigger>
          <TabsTrigger
            value="comments"
            className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <MessageCircle className="h-4 w-4" />
            댓글
          </TabsTrigger>
          <TabsTrigger
            value="likes"
            className="gap-2 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
          >
            <Heart className="h-4 w-4" />
            좋아요
          </TabsTrigger>
        </TabsList>

        <TabsContent value={activeTab} className="mt-0">
          {getFilteredNotifications().length > 0 ? (
            <div className="space-y-2">
              {getFilteredNotifications().map((notification) => (
                <div
                  key={notification.id}
                  className={`group flex items-start gap-4 rounded-lg border border-border p-4 transition-colors hover:bg-card ${
                    !notification.isRead ? "bg-primary/5" : "bg-card"
                  }`}
                >
                  {/* Icon */}
                  <div
                    className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${getNotificationColor(
                      notification.type
                    )}`}
                  >
                    {getNotificationIcon(notification.type)}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start gap-2">
                      <Avatar className="h-6 w-6">
                        <AvatarImage
                          src={notification.user.avatar}
                          alt={notification.user.name}
                        />
                        <AvatarFallback className="bg-secondary text-xs text-secondary-foreground">
                          {notification.user.name.slice(0, 2)}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1">
                        <p className="text-sm text-foreground">
                          <span className="font-semibold">{notification.user.name}</span>{" "}
                          {notification.content}
                        </p>
                        {notification.postTitle && (
                          <Link
                            href={`/post/${notification.postId}`}
                            className="mt-1 line-clamp-1 text-sm text-primary hover:underline"
                          >
                            {notification.postTitle}
                          </Link>
                        )}
                        <p className="mt-1 text-xs text-muted-foreground">
                          {notification.createdAt}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 opacity-0 transition-opacity group-hover:opacity-100">
                    {!notification.isRead && (
                      <div className="h-2 w-2 rounded-full bg-primary" />
                    )}
                    <button
                      onClick={() => deleteNotification(notification.id)}
                      className="text-muted-foreground hover:text-destructive"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="rounded-lg border border-border bg-card p-12 text-center">
              <Bell className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold text-foreground">
                알림이 없습니다
              </h3>
              <p className="text-sm text-muted-foreground">
                새로운 알림이 오면 여기에 표시됩니다
              </p>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
