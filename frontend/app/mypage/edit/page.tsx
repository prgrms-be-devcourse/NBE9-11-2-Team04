"use client"

import { ChangeEvent, FormEvent, useEffect, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Settings, Mail, User, ArrowLeft } from "lucide-react"
import { apiFetch } from "@/lib/api"
import { AUTH_CHANGED_EVENT, getAuthSnapshot } from "@/lib/auth-storage"

type MyProfileResponse = {
  userId: number
  email: string
  nickname: string
  role?: string
  status?: string
}

type UpdateMyProfileRequest = {
  email: string
  nickname: string
}

export default function MyPageEditPage() {
  const router = useRouter()

  const [form, setForm] = useState<UpdateMyProfileRequest>({
    email: "",
    nickname: "",
  })
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    const auth = getAuthSnapshot()

    if (!auth.token) {
      router.replace("/login")
      return
    }

    const fetchMyProfile = async () => {
      try {
        setLoading(true)
        setError("")

        const profile = await apiFetch<MyProfileResponse>("/api/mypage", {
          method: "GET",
          auth: true,
        })

        setForm({
          email: profile.email ?? "",
          nickname: profile.nickname ?? "",
        })
      } catch (err) {
        if (err instanceof Error && err.message === "UNAUTHORIZED") {
          router.replace("/login")
          return
        }

        console.error(err)
        setError("프로필 정보를 불러오지 못했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchMyProfile()
  }, [router])

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!form.email.trim()) {
      alert("이메일을 입력해주세요.")
      return
    }

    if (!form.nickname.trim()) {
      alert("닉네임을 입력해주세요.")
      return
    }

    try {
      setSubmitting(true)

      const updatedProfile = await apiFetch<MyProfileResponse>("/api/mypage", {
        method: "PATCH",
        auth: true,
        body: JSON.stringify({
          email: form.email.trim(),
          nickname: form.nickname.trim(),
        }),
      })

      if (typeof window !== "undefined") {
        const rawAuth = localStorage.getItem("auth")

        if (rawAuth) {
          const auth = JSON.parse(rawAuth)
          localStorage.setItem(
            "auth",
            JSON.stringify({
              ...auth,
              email: updatedProfile.email,
              nickname: updatedProfile.nickname,
            })
          )
        }

        window.dispatchEvent(new Event(AUTH_CHANGED_EVENT))
      }

      alert("프로필 수정이 완료되었습니다.")
      router.push("/mypage")
      router.refresh()
    } catch (err) {
      if (err instanceof Error && err.message === "UNAUTHORIZED") {
        router.replace("/login")
        return
      }

      console.error(err)
      alert(err instanceof Error ? err.message : "프로필 수정에 실패했습니다.")
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-border bg-card p-12 text-center">
          <p className="text-sm text-muted-foreground">로딩 중...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-border bg-card p-12 text-center">
          <p className="text-sm text-red-500">{error}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">프로필 수정</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            내 정보를 수정할 수 있습니다.
          </p>
        </div>

        <Link href="/mypage">
          <Button variant="outline" className="gap-2">
            <ArrowLeft className="h-4 w-4" />
            마이페이지로
          </Button>
        </Link>
      </div>

      <div className="rounded-lg border border-border bg-card p-6">
        <div className="flex flex-col items-start gap-6 sm:flex-row">
          <Avatar className="h-24 w-24 border-4 border-primary/20">
            <AvatarImage src="" alt={form.nickname || "사용자"} />
            <AvatarFallback className="bg-primary text-2xl text-primary-foreground">
              {(form.nickname || "사용자").slice(0, 2)}
            </AvatarFallback>
          </Avatar>

          <div className="flex-1">
            <div className="mb-4 flex flex-wrap items-center gap-4">
              <div>
                <h2 className="text-2xl font-bold text-foreground">
                  {form.nickname || "사용자"}
                </h2>
              </div>
              <Button variant="outline" className="gap-2" disabled>
                <Settings className="h-4 w-4" />
                프로필 편집 중
              </Button>
            </div>

            <p className="leading-relaxed text-muted-foreground">
              이메일과 닉네임을 수정한 뒤 저장 버튼을 누르면 마이페이지와 DB에 함께 반영됩니다.
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-6 border-t border-border pt-6">
          <div className="grid gap-6 md:grid-cols-2">
            <div>
              <label
                htmlFor="email"
                className="mb-2 flex items-center gap-2 text-sm font-medium text-foreground"
              >
                <Mail className="h-4 w-4" />
                이메일
              </label>
              <input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="이메일을 입력하세요"
                className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none transition focus:border-primary"
              />
            </div>

            <div>
              <label
                htmlFor="nickname"
                className="mb-2 flex items-center gap-2 text-sm font-medium text-foreground"
              >
                <User className="h-4 w-4" />
                닉네임
              </label>
              <input
                id="nickname"
                name="nickname"
                type="text"
                value={form.nickname}
                onChange={handleChange}
                placeholder="닉네임을 입력하세요"
                className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none transition focus:border-primary"
              />
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="submit"
              disabled={submitting}
              className="bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {submitting ? "수정 중..." : "저장"}
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={() => router.push("/mypage")}
            >
              취소
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}