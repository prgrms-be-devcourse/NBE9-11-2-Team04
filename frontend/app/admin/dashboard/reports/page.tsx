"use client"

import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  CheckCircle,
  XCircle,
  Clock,
  FileText,
  MessageSquare,
  User,
  Eye,
  MoreHorizontal,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { Card, CardContent } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

import {
  getReports,
  processReport,
  Report,
  ReportStatus,
} from "./api"

/* ---------------- label mapping ---------------- */

const typeLabels: Record<string, { label: string; icon: any }> = {
  POST: { label: "게시글", icon: FileText },
  COMMENT: { label: "댓글", icon: MessageSquare },
}

const reasonLabels: Record<string, string> = {
  SPAM: "스팸/광고",
  ABUSE: "욕설/비방",
  HATE: "혐오",
  ETC: "기타",
}

const statusLabels: Record<string, string> = {
  PENDING: "대기중",
  RESOLVED: "처리완료",
  REJECTED: "반려",
}

/* ---------------- page ---------------- */

export default function ReportsPage() {
  const qc = useQueryClient()

  const [selected, setSelected] = useState<Report | null>(null)
  const [note, setNote] = useState("")
  const [dialogOpen, setDialogOpen] = useState(false)
  const [action, setAction] = useState<"RESOLVED" | "REJECTED" | null>(null)

  /* -------- data fetch -------- */
  const { data, isLoading } = useQuery({
    queryKey: ["reports"],
    queryFn: () => getReports(),
  })

  const reports = data?.reports ?? []

  const pending = reports.filter((r) => r.status === "PENDING")
  const resolved = reports.filter((r) => r.status === "RESOLVED")
  const rejected = reports.filter((r) => r.status === "REJECTED")

  /* -------- mutation -------- */
  const mutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: "RESOLVED" | "REJECTED" }) =>
      processReport(id, { status, adminNote: note }),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["reports"] })
      setDialogOpen(false)
      setSelected(null)
      setNote("")
    },
  })

  const openAction = (report: Report, status: "RESOLVED" | "REJECTED") => {
    setSelected(report)
    setAction(status)
    setDialogOpen(true)
  }

  const handleSubmit = () => {
    if (!selected || !action) return

    mutation.mutate({
      id: selected.reportId,
      status: action,
    })
  }

  /* ---------------- table ---------------- */

  const TableView = ({ list }: { list: Report[] }) => (
    <div className="rounded-lg border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>유형</TableHead>
            <TableHead>사유</TableHead>
            <TableHead>대상ID</TableHead>
            <TableHead>신고자</TableHead>
            <TableHead>상태</TableHead>
            <TableHead />
          </TableRow>
        </TableHeader>

        <TableBody>
          {list.map((r) => {
            const TypeIcon = typeLabels[r.targetType].icon

            return (
              <TableRow key={r.reportId}>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <TypeIcon className="h-4 w-4" />
                    {typeLabels[r.targetType].label}
                  </div>
                </TableCell>

                <TableCell>{reasonLabels[r.reasonType]}</TableCell>

                <TableCell>{r.targetId}</TableCell>

                <TableCell>{r.reporter.name}</TableCell>

                <TableCell>{statusLabels[r.status]}</TableCell>

                <TableCell className="text-right">
                  <Button size="icon" variant="ghost" onClick={() => setSelected(r)}>
                    <Eye />
                  </Button>

                  {r.status === "PENDING" && (
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button size="icon" variant="ghost">
                          <MoreHorizontal />
                        </Button>
                      </DropdownMenuTrigger>

                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => openAction(r, "RESOLVED")}>
                          처리
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => openAction(r, "REJECTED")}>
                          반려
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  )}
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )

  /* ---------------- UI ---------------- */

  return (
    <div className="space-y-6">

      <h1 className="text-xl font-bold">Report 관리</h1>

      <div className="grid grid-cols-3 gap-4">
        <Card><CardContent>대기: {pending.length}</CardContent></Card>
        <Card><CardContent>처리: {resolved.length}</CardContent></Card>
        <Card><CardContent>반려: {rejected.length}</CardContent></Card>
      </div>

      <Tabs defaultValue="PENDING">
        <TabsList>
          <TabsTrigger value="PENDING">대기</TabsTrigger>
          <TabsTrigger value="RESOLVED">처리</TabsTrigger>
          <TabsTrigger value="REJECTED">반려</TabsTrigger>
        </TabsList>

        <TabsContent value="PENDING">
          <TableView list={pending} />
        </TabsContent>

        <TabsContent value="RESOLVED">
          <TableView list={resolved} />
        </TabsContent>

        <TabsContent value="REJECTED">
          <TableView list={rejected} />
        </TabsContent>
      </Tabs>

      {/* dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>처리</DialogTitle>
          </DialogHeader>

          <div className="space-y-2">
            <Label>메모</Label>
            <Textarea value={note} onChange={(e) => setNote(e.target.value)} />
          </div>

          <Button onClick={handleSubmit}>
            확인
          </Button>
        </DialogContent>
      </Dialog>

      {/* detail */}
      {selected && (
        <Dialog open onOpenChange={() => setSelected(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>상세</DialogTitle>
            </DialogHeader>

            <div className="space-y-2 text-sm">
              <div>type: {selected.targetType}</div>
              <div>reason: {selected.reasonType}</div>
              <div>detail: {selected.reasonDetail}</div>
              <div>user: {selected.reporter.name}</div>
            </div>
          </DialogContent>
        </Dialog>
      )}

    </div>
  )
}