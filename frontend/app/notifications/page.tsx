"use client"

import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import { Bell, MessageCircle, Heart, UserPlus, Check } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

const API_BASE_URL = "http://localhost:8080"

type NotificationType = "comment" | "reply" | "like" | "follow"

type NotificationItem = {
  notificationId: number
  userId: number
  actorUserId: number
  postId: number | null
  commentId: number | null
  type: string
  message: string
  isRead?: boolean
  read?: boolean
  createdAt: string
}

type NotificationListResponse = {
  notifications: NotificationItem[]
}

function normalizeNotification(notification: NotificationItem): NotificationItem {
  return {
    ...notification,
    isRead: notification.isRead ?? notification.read ?? false,
  }
}

function mapBackendType(type: string): NotificationType {
  switch (type.toUpperCase()) {
    case "COMMENT":
      return "comment"
    case "REPLY":
      return "reply"
    case "LIKE":
      return "like"
    case "FOLLOW":
      return "follow"
    default:
      return "comment"
  }
}

function getNotificationIcon(type: NotificationType) {
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

function getNotificationColor(type: NotificationType) {
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

function dispatchNotificationsUpdated() {
  if (typeof window === "undefined") {
    return
  }

  window.dispatchEvent(new CustomEvent("notifications-updated"))
}

function formatRelativeDate(createdAt: string) {
  const date = new Date(createdAt)

  if (Number.isNaN(date.getTime())) {
    return createdAt
  }

  const diffMs = Date.now() - date.getTime()
  const diffMinutes = Math.max(0, Math.floor(diffMs / 1000 / 60))

  if (diffMinutes < 1) return "방금 전"
  if (diffMinutes < 60) return `${diffMinutes}분 전`

  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}시간 전`

  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}일 전`

  return date.toLocaleString("ko-KR")
}

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [activeTab, setActiveTab] = useState("all")
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadNotifications = async () => {
    try {
      setLoading(true)
      setError(null)

      const response = await fetch(`${API_BASE_URL}/api/notifications`, {
        cache: "no-store",
      })

      if (!response.ok) {
        throw new Error("알림 목록을 불러오지 못했습니다.")
      }

      const data: NotificationListResponse = await response.json()
      setNotifications(data.notifications.map(normalizeNotification))
    } catch (fetchError) {
      const message =
        fetchError instanceof Error ? fetchError.message : "알림 요청 중 오류가 발생했습니다."
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadNotifications()
  }, [])

  const markAllAsRead = async () => {
    const unreadNotifications = notifications.filter((notification) => !notification.isRead)

    if (unreadNotifications.length === 0) return

    try {
      setError(null)

      await Promise.all(
        unreadNotifications.map(async (notification) => {
          const response = await fetch(
            `${API_BASE_URL}/api/notifications/${notification.notificationId}/read`,
            {
              method: "PATCH",
            }
          )

          if (!response.ok) {
            throw new Error("일부 알림 읽음 처리에 실패했습니다.")
          }
        })
      )

      await loadNotifications()
      dispatchNotificationsUpdated()
    } catch (readError) {
      const message =
        readError instanceof Error ? readError.message : "알림 읽음 처리 중 오류가 발생했습니다."
      setError(message)
    }
  }

  const markAsRead = async (notificationId: number) => {
    try {
      setError(null)

      const response = await fetch(`${API_BASE_URL}/api/notifications/${notificationId}/read`, {
        method: "PATCH",
      })

      if (!response.ok) {
        throw new Error("알림 읽음 처리에 실패했습니다.")
      }

      await loadNotifications()
      dispatchNotificationsUpdated()
    } catch (readError) {
      const message =
        readError instanceof Error ? readError.message : "알림 읽음 처리 중 오류가 발생했습니다."
      setError(message)
    }
  }

  const unreadCount = notifications.filter((notification) => !notification.isRead).length

  const commentNotifications = useMemo(
    () =>
      notifications.filter((notification) => {
        const mappedType = mapBackendType(notification.type)
        return mappedType === "comment" || mappedType === "reply"
      }),
    [notifications]
  )

  const likeNotifications = useMemo(
    () => notifications.filter((notification) => mapBackendType(notification.type) === "like"),
    [notifications]
  )

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
      <div className="mb-8 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
            <Bell className="h-5 w-5 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-foreground">알림</h1>
            {unreadCount > 0 ? (
              <p className="text-sm text-muted-foreground">읽지 않은 알림 {unreadCount}개</p>
            ) : (
              <p className="text-sm text-muted-foreground">모든 알림을 확인했어요</p>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={() => void loadNotifications()}>
            새로고침
          </Button>
          {unreadCount > 0 && (
            <Button variant="outline" onClick={() => void markAllAsRead()} className="gap-2">
              <Check className="h-4 w-4" />
              모두 읽음
            </Button>
          )}
        </div>
      </div>

      {error ? (
        <div className="mb-6 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

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
          {loading ? (
            <div className="rounded-lg border border-border bg-card p-12 text-center text-sm text-muted-foreground">
              알림을 불러오는 중입니다...
            </div>
          ) : getFilteredNotifications().length > 0 ? (
            <div className="space-y-2">
              {getFilteredNotifications().map((notification) => {
                const mappedType = mapBackendType(notification.type)

                return (
                  <div
                    key={notification.notificationId}
                    className={`group flex items-start gap-4 rounded-lg border border-border p-4 transition-colors hover:bg-card ${
                      !notification.isRead ? "bg-primary/5" : "bg-card"
                    }`}
                  >
                    <div
                      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${getNotificationColor(
                        mappedType
                      )}`}
                    >
                      {getNotificationIcon(mappedType)}
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="flex items-start gap-2">
                        <Avatar className="h-6 w-6">
                          <AvatarImage src={undefined} alt={`${notification.actorUserId}번 사용자`} />
                          <AvatarFallback className="bg-secondary text-xs text-secondary-foreground">
                            U{notification.actorUserId}
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex-1">
                          <p className="text-sm text-foreground">{notification.message}</p>
                          {notification.postId ? (
                            <Link
                              href={`/post/${notification.postId}`}
                              className="mt-1 line-clamp-1 text-sm text-primary hover:underline"
                            >
                              게시글 {notification.postId}번으로 이동
                            </Link>
                          ) : null}
                          <p className="mt-1 text-xs text-muted-foreground">
                            {formatRelativeDate(notification.createdAt)}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 opacity-100 transition-opacity sm:opacity-0 sm:group-hover:opacity-100">
                      {!notification.isRead && <div className="h-2 w-2 rounded-full bg-primary" />}
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        disabled={notification.isRead}
                        onClick={() => void markAsRead(notification.notificationId)}
                      >
                        읽음 처리
                      </Button>
                    </div>
                  </div>
                )
              })}
            </div>
          ) : (
            <div className="rounded-lg border border-border bg-card p-12 text-center">
              <Bell className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold text-foreground">알림이 없습니다</h3>
              <p className="text-sm text-muted-foreground">새로운 알림이 오면 여기에 표시됩니다</p>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
