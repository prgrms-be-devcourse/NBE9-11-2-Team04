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

const recentReports = [
  {
    id: "1",
    type: "게시글",
    reason: "스팸/광고",
    reporter: "김철수",
    target: "비트코인 투자 꿀팁...",
    date: "5분 전",
  },
  {
    id: "2",
    type: "댓글",
    reason: "욕설/비방",
    reporter: "이영희",
    target: "너 진짜...",
    date: "12분 전",
  },
  {
    id: "3",
    type: "사용자",
    reason: "사칭",
    reporter: "박민수",
    target: "@fake_developer",
    date: "1시간 전",
  },
]



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

      {/* Recent Reports */}
      <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-destructive" />
              최근 신고
            </CardTitle>
            <CardDescription>최근 접수된 신고 내역입니다.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentReports.map((report) => (
                <div
                  key={report.id}
                  className="flex items-center justify-between rounded-lg border border-border p-3"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="rounded bg-destructive/10 px-2 py-0.5 text-xs font-medium text-destructive">
                        {report.type}
                      </span>
                      <span className="text-sm font-medium text-foreground">
                        {report.reason}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground">
                      대상: {report.target}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-muted-foreground">{report.date}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
      </Card>

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
