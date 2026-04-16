"use client"

import { useEffect, useState } from "react"
import {
  CheckCircle,
  XCircle,
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
import { Textarea } from "@/components/ui/textarea"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

/* =========================
   API CONFIG
========================= */

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "")

/* =========================
   TYPES (BACKEND DTO MATCH)
========================= */

type ReportType = "POST" | "COMMENT"
type ReportStatus = "PENDING" | "RESOLVED" | "REJECTED"

interface Report {
  reportId: number

  reporterEmail: string
  reporterNickname: string

  targetType: ReportType
  targetId: number

  targetNickname: string
  targetTitle?: string
  targetContent?: string

  reasonType: string
  reasonDetail: string

  status: ReportStatus

  createdAt: string
  processedAt?: string
}

/* =========================
   LABELS
========================= */

const typeLabels: Record<ReportType, { label: string; icon: any }> = {
  POST: { label: "게시글", icon: FileText },
  COMMENT: { label: "댓글", icon: MessageSquare },
}

const statusLabels: Record<
  ReportStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: "대기",
    className: "bg-yellow-500/10 text-yellow-500",
  },
  RESOLVED: {
    label: "처리완료",
    className: "bg-green-500/10 text-green-500",
  },
  REJECTED: {
    label: "반려",
    className: "bg-muted text-muted-foreground",
  },
}

/* =========================
   API
========================= */

async function fetchReports(): Promise<Report[]> {
  const token = localStorage.getItem("accessToken")

  const res = await fetch(`${API_BASE}/api/admin/reports/pending`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  })

  if (!res.ok) throw new Error("신고 조회 실패")

  const json = await res.json()
  return json.data.content
}

async function processReportApi(params: {
  reportId: number
  action: "RESOLVE" | "REJECT"
  adminNote: string
  sanctionType?: string
}) {
  const res = await fetch(
    `${API_BASE}/api/admin/reports/${params.reportId}/process`,
    {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(params),
    }
  )

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
    action: "RESOLVE" | "REJECT" | null
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

  const openProcessDialog = (
    report: Report,
    action: "RESOLVE" | "REJECT"
  ) => {
    setProcessDialog({ open: true, report, action })
    setAdminNote("")
    setSanctionType("")
  }

  const processReport = async () => {
    if (!processDialog.report || !processDialog.action) return

    try {
      await processReportApi({
        reportId: processDialog.report.reportId,
        action: processDialog.action,
        adminNote,
        sanctionType,
      })

      setReports((prev) =>
        prev.map((r) =>
          r.reportId === processDialog.report!.reportId
            ? {
              ...r,
              status:
                processDialog.action === "RESOLVE"
                  ? "RESOLVED"
                  : "REJECTED",
            }
            : r
        )
      )

      setProcessDialog({ open: false, report: null, action: null })
    } catch {
      alert("처리 실패")
    }
  }

  /* =========================
     FILTER
  ========================= */

  const pendingReports = reports.filter((r) => r.status === "PENDING")
  const resolvedReports = reports.filter((r) => r.status === "RESOLVED")
  const rejectedReports = reports.filter((r) => r.status === "REJECTED")

  if (loading) return <div className="p-6">로딩중...</div>
  if (error) return <div className="p-6 text-red-500">{error}</div>

  /* =========================
     TABLE (UI 그대로 유지)
  ========================= */

  const ReportTable = ({
    data,
    showActions,
  }: {
    data: Report[]
    showActions: boolean
  }) => (
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
          {data.map((r) => {
            const TypeIcon = typeLabels[r.targetType].icon

            return (
              <TableRow key={r.reportId}>
                <TableCell>
                  {typeLabels[r.targetType].label}
                </TableCell>

                <TableCell>{r.reasonType}</TableCell>

                <TableCell className="max-w-[300px] truncate">
                  {r.targetTitle || r.targetContent}
                </TableCell>

                <TableCell>{r.reporterNickname}</TableCell>

                <TableCell>
                  <span
                    className={statusLabels[r.status].className}
                  >
                    {statusLabels[r.status].label}
                  </span>
                </TableCell>

                {showActions && (
                  <TableCell>
                    <Button
                      size="icon"
                      variant="ghost"
                      onClick={() => setSelectedReport(r)}
                    >
                      <Eye />
                    </Button>

                    {r.status === "PENDING" && (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button size="icon" variant="ghost">
                            <MoreHorizontal />
                          </Button>
                        </DropdownMenuTrigger>

                        <DropdownMenuContent>
                          <DropdownMenuItem
                            onClick={() =>
                              openProcessDialog(r, "RESOLVE")
                            }
                          >
                            처리
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onClick={() =>
                              openProcessDialog(r, "REJECT")
                            }
                          >
                            반려
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    )}
                  </TableCell>
                )}
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )

  /* =========================
     UI (UNCHANGED)
  ========================= */

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">신고 관리</h1>

      <Tabs defaultValue="pending">
        <TabsList>
          <TabsTrigger value="pending">
            대기 ({pendingReports.length})
          </TabsTrigger>
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

      {/* PROCESS MODAL (UNCHANGED UI STRUCTURE) */}
      <Dialog
        open={processDialog.open}
        onOpenChange={() =>
          setProcessDialog({ open: false, report: null, action: null })
        }
      >
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

          <Textarea
            value={adminNote}
            onChange={(e) => setAdminNote(e.target.value)}
          />

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() =>
                setProcessDialog({
                  open: false,
                  report: null,
                  action: null,
                })
              }
            >
              취소
            </Button>
            <Button onClick={processReport}>확인</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* DETAIL */}
      <Dialog
        open={!!selectedReport}
        onOpenChange={() => setSelectedReport(null)}
      >
        <DialogContent>
          <DialogTitle>상세</DialogTitle>
          {selectedReport?.reasonDetail}
        </DialogContent>
      </Dialog>
    </div>
  )
}