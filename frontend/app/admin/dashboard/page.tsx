"use client"

import { Users, FileText, AlertTriangle, TrendingUp, Eye, MessageSquare } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"

const stats = [
  {
    name: "총 사용자",
    value: "12,458",
    change: "+12%",
    trend: "up",
    icon: Users,
  },
  {
    name: "총 게시글",
    value: "3,842",
    change: "+8%",
    trend: "up",
    icon: FileText,
  },
  {
    name: "미처리 신고",
    value: "24",
    change: "-5%",
    trend: "down",
    icon: AlertTriangle,
  },
  {
    name: "일일 방문자",
    value: "2,156",
    change: "+18%",
    trend: "up",
    icon: Eye,
  },
]

// 오늘 신고 통계 데이터
const todayReportStats = {
  post: {
    total: 12,
    pending: 8,
    resolved: 4,
    byReason: [
      { reason: "스팸/광고", count: 5 },
      { reason: "욕설/비방", count: 3 },
      { reason: "허위정보", count: 2 },
      { reason: "개인정보 노출", count: 1 },
      { reason: "기타", count: 1 },
    ],
  },
  comment: {
    total: 6,
    pending: 4,
    resolved: 2,
    byReason: [
      { reason: "욕설/비방", count: 3 },
      { reason: "스팸/광고", count: 2 },
      { reason: "개인정보 노출", count: 1 },
    ],
  },
}



export default function AdminDashboardPage() {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold text-foreground">대시보드</h1>
        <p className="text-muted-foreground">DevHub 관리자 대시보드에 오신 것을 환영합니다.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <Card key={stat.name}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.name}
              </CardTitle>
              <stat.icon className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-foreground">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                <span
                  className={
                    stat.trend === "up" ? "text-green-500" : "text-destructive"
                  }
                >
                  {stat.change}
                </span>{" "}
                지난 달 대비
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Today Report Stats */}
      <div>
        <h2 className="mb-4 text-lg font-semibold text-foreground flex items-center gap-2">
          <AlertTriangle className="h-5 w-5 text-destructive" />
          오늘 신고 현황
        </h2>
        <div className="grid gap-4 md:grid-cols-2">
          {/* Post Report Card */}
          <Card>
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2 text-base">
                  <FileText className="h-4 w-4 text-primary" />
                  게시글 신고
                </CardTitle>
                <div className="flex items-center gap-4 text-sm">
                  <span className="font-semibold text-foreground">{todayReportStats.post.total}건</span>
                  <span className="text-amber-500">대기 {todayReportStats.post.pending}</span>
                  <span className="text-green-500">처리 {todayReportStats.post.resolved}</span>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {todayReportStats.post.byReason.map((item) => (
                  <div key={item.reason} className="flex items-center justify-between rounded-md bg-secondary/50 px-3 py-2">
                    <span className="text-sm text-muted-foreground">{item.reason}</span>
                    <span className="text-sm font-medium text-foreground">{item.count}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Comment Report Card */}
          <Card>
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2 text-base">
                  <MessageSquare className="h-4 w-4 text-primary" />
                  댓글 신고
                </CardTitle>
                <div className="flex items-center gap-4 text-sm">
                  <span className="font-semibold text-foreground">{todayReportStats.comment.total}건</span>
                  <span className="text-amber-500">대기 {todayReportStats.comment.pending}</span>
                  <span className="text-green-500">처리 {todayReportStats.comment.resolved}</span>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {todayReportStats.comment.byReason.map((item) => (
                  <div key={item.reason} className="flex items-center justify-between rounded-md bg-secondary/50 px-3 py-2">
                    <span className="text-sm text-muted-foreground">{item.reason}</span>
                    <span className="text-sm font-medium text-foreground">{item.count}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Quick Stats */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            오늘의 활동
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 sm:grid-cols-3">
            <div className="rounded-lg bg-secondary p-4 text-center">
              <FileText className="mx-auto mb-2 h-6 w-6 text-primary" />
              <p className="text-2xl font-bold text-foreground">156</p>
              <p className="text-sm text-muted-foreground">새 게시글</p>
            </div>
            <div className="rounded-lg bg-secondary p-4 text-center">
              <MessageSquare className="mx-auto mb-2 h-6 w-6 text-primary" />
              <p className="text-2xl font-bold text-foreground">423</p>
              <p className="text-sm text-muted-foreground">새 댓글</p>
            </div>
            <div className="rounded-lg bg-secondary p-4 text-center">
              <Users className="mx-auto mb-2 h-6 w-6 text-primary" />
              <p className="text-2xl font-bold text-foreground">28</p>
              <p className="text-sm text-muted-foreground">신규 가입</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
