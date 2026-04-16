"use client"

import { useEffect, useState } from "react"
import {
  CheckCircle,
  XCircle,
  Clock,
  FileText,
  MessageSquare,
  User,
  MoreHorizontal,
  Eye,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

/* =========================
   API CONFIG
========================= */

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "")

/* =========================
   TYPES
========================= */

type ReportType = "post" | "comment" | "user"
type ReportStatus = "pending" | "resolved" | "rejected"
type ReportReason = "spam" | "abuse" | "inappropriate" | "impersonation" | "other"

interface Report {
  reportId: number
  targetType: ReportType
  reasonType: ReportReason
  reasonDetail: string
  reporter: { reporterNickname: string; reporterEmail: string }
  target: {
    targetType: ReportType
    targetId: string
    title?: string
    content?: string
    author?: string
  }
  status: ReportStatus
  createdAt: string
}

/* =========================
   LABELS
========================= */

const reasonLabels: Record<ReportReason, string> = {
  spam: "스팸/광고",
  abuse: "욕설/비방",
  inappropriate: "부적절",
  impersonation: "사칭",
  other: "기타",
}

const typeLabels: Record<ReportType, { label: string; icon: any }> = {
  post: { label: "게시글", icon: FileText },
  comment: { label: "댓글", icon: MessageSquare },
  user: { label: "사용자", icon: User },
}

const statusLabels: Record<ReportStatus, { label: string; className: string }> = {
  pending: { label: "대기", className: "bg-yellow-500/10 text-yellow-500" },
  resolved: { label: "처리완료", className: "bg-green-500/10 text-green-500" },
  rejected: { label: "반려", className: "bg-muted text-muted-foreground" },
}

/* =========================
   API
========================= */

async function fetchReports(): Promise<Report[]> {
  const res = await fetch(`${API_BASE}/api/admin/reports`)
  if (!res.ok) throw new Error("신고 조회 실패")

  const data = await res.json()
  return data.data
}

async function processReportApi(params: {
  id: string
  action: "resolve" | "reject"
  adminNote: string
  sanctionType?: string
}) {
  const res = await fetch(`${API_BASE}/api/admin/reports/${params.id}/process`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(params),
  })

  if (!res.ok) throw new Error("처리 실패")
  return res.json()
}

/* =========================
   PAGE
========================= */

export default function ReportsManagementPage() {
  const [reports, setReports] = useState<Report[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [selectedReport, setSelectedReport] = useState<Report | null>(null)

  const [processDialog, setProcessDialog] = useState<{
    open: boolean
    report: Report | null
    action: "resolve" | "reject" | null
  }>({ open: false, report: null, action: null })

  const [adminNote, setAdminNote] = useState("")
  const [sanctionType, setSanctionType] = useState("")

  /* =========================
     LOAD
  ========================= */

  const loadReports = async () => {
    try {
      setLoading(true)
      setError(null)

      const data = await fetchReports()
      setReports(data)
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadReports()
  }, [])

  /* =========================
     PROCESS
  ========================= */

  const openProcessDialog = (report: Report, action: "resolve" | "reject") => {
    setProcessDialog({ open: true, report, action })
    setAdminNote("")
    setSanctionType("")
  }

  const processReport = async () => {
    if (!processDialog.report || !processDialog.action) return

    try {
      await processReportApi({
        id: processDialog.report.id,
        action: processDialog.action,
        adminNote,
        sanctionType,
      })

      setReports((prev) =>
        prev.map((r) =>
          r.id === processDialog.report!.id
            ? { ...r, status: processDialog.action === "resolve" ? "resolved" : "rejected" }
            : r
        )
      )

      setProcessDialog({ open: false, report: null, action: null })
    } catch (e) {
      alert("처리 실패")
    }
  }

  /* =========================
     FILTERS
  ========================= */

  const pendingReports = reports.filter((r) => r.status === "pending")
  const resolvedReports = reports.filter((r) => r.status === "resolved")
  const rejectedReports = reports.filter((r) => r.status === "rejected")

  if (loading) return <div className="p-6">로딩중...</div>
  if (error) return <div className="p-6 text-red-500">{error}</div>

  /* =========================
     TABLE
  ========================= */

  const ReportTable = ({ data, showActions }: { data: Report[]; showActions: boolean }) => (
    <div className="border rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>유형</TableHead>
            <TableHead>사유</TableHead>
            <TableHead>내용</TableHead>
            <TableHead>신고자</TableHead>
            <TableHead>상태</TableHead>
            {showActions && <TableHead>작업</TableHead>}
          </TableRow>
        </TableHeader>

        <TableBody>
          {data.map((r) => (
            <TableRow key={r.id}>
              <TableCell>{typeLabels[r.type].label}</TableCell>
              <TableCell>{reasonLabels[r.reason]}</TableCell>
              <TableCell className="max-w-[300px] truncate">
                {r.target.title || r.target.content}
              </TableCell>
              <TableCell>{r.reporter.name}</TableCell>
              <TableCell>
                <span className={statusLabels[r.status].className}>
                  {statusLabels[r.status].label}
                </span>
              </TableCell>

              {showActions && (
                <TableCell>
                  <Button size="icon" variant="ghost" onClick={() => setSelectedReport(r)}>
                    <Eye />
                  </Button>

                  {r.status === "pending" && (
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button size="icon" variant="ghost">
                          <MoreHorizontal />
                        </Button>
                      </DropdownMenuTrigger>

                      <DropdownMenuContent>
                        <DropdownMenuItem onClick={() => openProcessDialog(r, "resolve")}>
                          처리
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => openProcessDialog(r, "reject")}>
                          반려
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  )}
                </TableCell>
              )}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )

  /* =========================
     UI
  ========================= */

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">신고 관리</h1>

      <Tabs defaultValue="pending">
        <TabsList>
          <TabsTrigger value="pending">대기 ({pendingReports.length})</TabsTrigger>
          <TabsTrigger value="resolved">완료</TabsTrigger>
          <TabsTrigger value="rejected">반려</TabsTrigger>
        </TabsList>

        <TabsContent value="pending">
          <ReportTable data={pendingReports} showActions />
        </TabsContent>

        <TabsContent value="resolved">
          <ReportTable data={resolvedReports} showActions={false} />
        </TabsContent>

        <TabsContent value="rejected">
          <ReportTable data={rejectedReports} showActions={false} />
        </TabsContent>
      </Tabs>

      {/* PROCESS MODAL */}
      <Dialog open={processDialog.open} onOpenChange={() => setProcessDialog({ open: false, report: null, action: null })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>처리</DialogTitle>
          </DialogHeader>

          <Select value={sanctionType} onValueChange={setSanctionType}>
            <SelectTrigger>
              <SelectValue placeholder="제재 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="warn">경고</SelectItem>
              <SelectItem value="ban">차단</SelectItem>
              <SelectItem value="delete">삭제</SelectItem>
            </SelectContent>
          </Select>

          <Textarea value={adminNote} onChange={(e) => setAdminNote(e.target.value)} />

          <DialogFooter>
            <Button variant="outline" onClick={() => setProcessDialog({ open: false, report: null, action: null })}>
              취소
            </Button>
            <Button onClick={processReport}>확인</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* DETAIL */}
      <Dialog open={!!selectedReport} onOpenChange={() => setSelectedReport(null)}>
        <DialogContent>
          <DialogTitle>상세</DialogTitle>
          {selectedReport?.description}
        </DialogContent>
      </Dialog>
    </div>
  )
}