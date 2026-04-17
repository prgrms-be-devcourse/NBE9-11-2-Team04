"use client"

import { useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { persistLoginSession } from "@/lib/auth-storage"

export default function OAuthCallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const oauth = searchParams.get("oauth")
    const email = searchParams.get("email")
    const nickname = searchParams.get("nickname")
    const errorCode = searchParams.get("errorCode")

    // 오류 발생 시 로그인 페이지로 리디렉션
    if (oauth === "error") {
      const query = new URLSearchParams()
      query.set("oauth", "error")

      if (errorCode) {
        query.set("errorCode", errorCode)
      }

      router.replace(`/login?${query.toString()}`)
      return
    }

    // 로그인 성공 시 메인 페이지로 리디렉션
    if (oauth === "success") {
      if (email && nickname) {
        persistLoginSession(undefined, nickname, email)
        router.replace("/main") // 메인 페이지로 리디렉션
      } else {
        router.replace("/login") // 이메일이나 닉네임이 없으면 로그인 페이지로
      }
      return
    }

    // 기본적으로 로그인 페이지로 리디렉션
    router.replace("/login")
  }, [router, searchParams])

  return (
    <div className="mx-auto flex min-h-[60vh] max-w-4xl items-center justify-center px-4 py-8 sm:px-6 lg:px-8">
      <div className="w-full max-w-md rounded-lg border border-border bg-card p-8 text-center shadow-sm">
        <h1 className="text-xl font-semibold text-foreground">OAuth 로그인 처리 중</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          로그인 정보를 확인하고 있습니다. 잠시만 기다려주세요.
        </p>
      </div>
    </div>
  )
}