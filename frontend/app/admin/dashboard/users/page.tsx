"use client"

import { useEffect, useState } from "react"
import {
  Search,
  Filter,
  MoreHorizontal,
  UserCheck,
  Eye,
  Ban,
  Clock,
  Mail,
  ChevronLeft,
  ChevronRight,
} from "lucide-react"

import { useDebounce } from "@/hooks/useDebounce"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogFooter,
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

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "")

/**
 * =========================
 * TYPES
 * =========================
 */

type UserStatus = "ACTIVE" | "WARNED" | "BLACKLISTED"

interface User {
  userId: number
  nickname: string
  email: string
  createdAt: string
  postCount: number
  commentCount: number
  status: UserStatus
}

interface ApiResponse<T> {
  data: T
}

interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
}

/**
 * =========================
 * API LAYER
 * =========================
 */

async function fetchUsers(params: {
  page: number
  size: number
  keyword?: string
  status?: string
}): Promise<PageResponse<User>> {
  if (!API_BASE) throw new Error("API_BASE 환경변수 없음")

  const token = localStorage.getItem("accessToken")
  if (!token) throw new Error("로그인 토큰 없음")

  const query = new URLSearchParams()
  query.append("page", String(params.page))
  query.append("size", String(params.size))

  if (params.keyword) query.append("keyword", params.keyword)
  if (params.status && params.status !== "all")
    query.append("status", params.status)

  const res = await fetch(`${API_BASE}/api/admin/members?${query.toString()}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  })

  if (!res.ok) throw new Error("사용자 조회 실패")

  const body: ApiResponse<PageResponse<User>> = await res.json()
  return body.data
}

async function updateUserStatus(params: {
  userId: number
  status: UserStatus
}) {
  if (!API_BASE) throw new Error("API_BASE 환경변수 없음")

  const token = localStorage.getItem("accessToken")
  if (!token) throw new Error("로그인 토큰 없음")

  const res = await fetch(
    `${API_BASE}/api/admin/members/${params.userId}/status`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ status: params.status }),
    }
  )

  if (!res.ok) throw new Error("상태 변경 실패")

  return res.json()
}

/**
 * =========================
 * STATUS UI MAP
 * =========================
 */

const statusLabels: Record<UserStatus, { label: string; className: string }> =
{
  ACTIVE: { label: "활성", className: "bg-green-500/10 text-green-500" },
  WARNED: { label: "경고", className: "bg-yellow-500/10 text-yellow-500" },
  BLACKLISTED: {
    label: "차단",
    className: "bg-destructive/10 text-destructive",
  },
}

/**
 * =========================
 * PAGE
 * =========================
 */

export default function UsersManagementPage() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const debouncedSearchQuery = useDebounce(searchQuery, 300)
  const debouncedStatusFilter = useDebounce(statusFilter, 300)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)

  const [actionDialog, setActionDialog] = useState<{
    open: boolean
    type: "warn" | "blacklist" | "activate" | null
    user: User | null
  }>({ open: false, type: null, user: null })

  // const [sanctionReason, setSanctionReason] = useState("")
  // const [sanctionDuration, setSanctionDuration] = useState("7")

  // pagination
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const loadUsers = async () => {
    try {
      setError(null)

      const pageData = await fetchUsers({
        page,
        size,
        keyword: debouncedSearchQuery,
        status: debouncedStatusFilter,
      })

      setUsers(pageData.content)
      setTotalPages(pageData.totalPages)
      setTotalElements(pageData.totalElements)
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  // page/size 변경시 로딩
  useEffect(() => {
    loadUsers()
  }, [page, size])

  // 검색/필터 변경시 page=0으로 초기화
  useEffect(() => {
    setPage(0)
  }, [searchQuery, statusFilter])

  // 검색/필터 변경시 다시 로딩
  useEffect(() => {
    loadUsers()
  }, [page, size, debouncedSearchQuery, debouncedStatusFilter])

  const handleAction = (
    type: "warn" | "blacklist" | "activate",
    user: User
  ) => {
    setActionDialog({ open: true, type, user })
    // setSanctionReason("")
    // setSanctionDuration("7")
  }

  const confirmAction = async () => {
    if (!actionDialog.user || !actionDialog.type) return

    const userId = actionDialog.user.userId

    const newStatus: UserStatus =
      actionDialog.type === "activate"
        ? "ACTIVE"
        : actionDialog.type === "warn"
          ? "WARNED"
          : "BLACKLISTED"

    try {
      await updateUserStatus({
        userId,
        status: newStatus,
      })

      setUsers((prev) =>
        prev.map((u) => (u.userId === userId ? { ...u, status: newStatus } : u))
      )

      setActionDialog({ open: false, type: null, user: null })
    } catch (e) {
      alert("처리 실패")
    }
  }

  if (!API_BASE) return <div className="p-6">API_BASE 환경변수 없음</div>
  if (loading) return <div className="p-6">로딩 중...</div>
  if (error) return <div className="p-6 text-red-500">{error}</div>

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div>
        <h1 className="text-2xl font-bold">사용자 관리</h1>
        <p className="text-muted-foreground">회원 목록 조회 및 상태 관리</p>
      </div>

      {/* FILTER */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-1 gap-2">
          <div className="relative flex-1 sm:max-w-xs">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="닉네임 / 이메일 검색"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>

          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-40">
              <Filter className="mr-2 h-4 w-4" />
              <SelectValue placeholder="상태" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">전체</SelectItem>
              <SelectItem value="ACTIVE">활성</SelectItem>
              <SelectItem value="WARNED">경고</SelectItem>
              <SelectItem value="BLACKLISTED">차단</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <p className="text-sm text-muted-foreground">
          총 {totalElements}명 (페이지 {page + 1}/{totalPages})
        </p>
      </div>

      {/* TABLE */}
      <div className="rounded-lg border overflow-hidden">
        <table className="w-full">
          <thead className="bg-secondary">
            <tr>
              <th className="p-3 text-left">사용자</th>
              <th className="p-3 text-left">활동</th>
              <th className="p-3 text-left">상태</th>
              <th className="p-3 text-right">작업</th>
            </tr>
          </thead>

          <tbody>
            {users.map((user) => (
              <tr key={user.userId} className="border-t">
                <td className="p-3">
                  <div>
                    <p className="font-medium">{user.nickname}</p>
                    <p className="text-xs text-muted-foreground">{user.email}</p>
                  </div>
                </td>

                <td className="p-3 text-sm">
                  게시글 {user.postCount} / 댓글 {user.commentCount}
                </td>

                <td className="p-3">
                  <span
                    className={`px-2 py-1 text-xs rounded ${statusLabels[user.status].className}`}
                  >
                    {statusLabels[user.status].label}
                  </span>
                </td>

                <td className="p-3 text-right">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon">
                        <MoreHorizontal />
                      </Button>
                    </DropdownMenuTrigger>

                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => setSelectedUser(user)}>
                        <Eye className="mr-2 h-4 w-4" />
                        상세
                      </DropdownMenuItem>

                      <DropdownMenuItem>
                        <Mail className="mr-2 h-4 w-4" />
                        이메일
                      </DropdownMenuItem>

                      <DropdownMenuSeparator />

                      {user.status !== "ACTIVE" && (
                        <DropdownMenuItem
                          onClick={() => handleAction("activate", user)}
                        >
                          <UserCheck className="mr-2 h-4 w-4" />
                          활성화
                        </DropdownMenuItem>
                      )}

                      {user.status === "ACTIVE" && (
                        <DropdownMenuItem
                          onClick={() => handleAction("warn", user)}
                        >
                          <Clock className="mr-2 h-4 w-4" />
                          경고
                        </DropdownMenuItem>
                      )}

                      {user.status !== "BLACKLISTED" && (
                        <DropdownMenuItem
                          onClick={() => handleAction("blacklist", user)}
                        >
                          <Ban className="mr-2 h-4 w-4" />
                          차단
                        </DropdownMenuItem>
                      )}
                    </DropdownMenuContent>
                  </DropdownMenu>
                </td>
              </tr>
            ))}

            {users.length === 0 && (
              <tr>
                <td colSpan={4} className="p-6 text-center text-muted-foreground">
                  결과 없음
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* PAGINATION */}
      <div className="flex items-center justify-between">
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((prev) => prev - 1)}
          >
            <ChevronLeft className="mr-1 h-4 w-4" />
            이전
          </Button>

          <Button
            variant="outline"
            size="sm"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((prev) => prev + 1)}
          >
            다음
            <ChevronRight className="ml-1 h-4 w-4" />
          </Button>
        </div>

        <Select value={String(size)} onValueChange={(v) => setSize(Number(v))}>
          <SelectTrigger className="w-32">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="5">5개</SelectItem>
            <SelectItem value="10">10개</SelectItem>
            <SelectItem value="20">20개</SelectItem>
            <SelectItem value="50">50개</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* DETAIL DIALOG */}
      <Dialog open={!!selectedUser} onOpenChange={() => setSelectedUser(null)}>
        <DialogContent>
          <DialogTitle>사용자 상세</DialogTitle>

          {selectedUser && (
            <div className="space-y-2">
              <p>{selectedUser.nickname}</p>
              <p>{selectedUser.email}</p>
              <p>게시글 {selectedUser.postCount}</p>
              <p>댓글 {selectedUser.commentCount}</p>
              <p>상태: {statusLabels[selectedUser.status].label}</p>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* ACTION DIALOG */}
      <Dialog
        open={actionDialog.open}
        onOpenChange={(open) =>
          !open && setActionDialog({ open: false, type: null, user: null })
        }
      >
        <DialogContent>
          <DialogTitle>처리</DialogTitle>

          {actionDialog.type !== "activate" && (
            <div className="space-y-3">
              {actionDialog.type === "warn" && (
                <Select
                // value={sanctionDuration}
                // onValueChange={setSanctionDuration}
                >
                  <SelectTrigger />
                  <SelectContent>
                    <SelectItem value="1">1일</SelectItem>
                    <SelectItem value="7">7일</SelectItem>
                    <SelectItem value="30">30일</SelectItem>
                  </SelectContent>
                </Select>
              )}

              <Textarea
                placeholder="사유"
              // value={sanctionReason}
              // onChange={(e) => setSanctionReason(e.target.value)}
              />
            </div>
          )}

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() =>
                setActionDialog({ open: false, type: null, user: null })
              }
            >
              취소
            </Button>

            <Button onClick={confirmAction}>확인</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}