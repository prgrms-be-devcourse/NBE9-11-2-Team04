"use client"

import { useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { persistLoginSession } from "@/lib/auth-storage"
import { exchangeOAuthCode } from "@/lib/auth"

export default function OAuthCallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const oauth = searchParams.get("oauth")
    const errorCode = searchParams.get("errorCode")
    const code = searchParams.get("code")

    const run = async () => {
      // 1. 에러 발생 시 처리
      if (oauth === "error") {
        const query = new URLSearchParams()
        query.set("oauth", "error")
        if (errorCode) query.set("errorCode", errorCode)
        
        router.replace(`/login?${query.toString()}`)
        return
      }

      // 2. 로그인 성공 및 코드가 있을 때 처리
      if (oauth === "success" && code) {
        try {
          // 서버에서 토큰 교환
          const data = await exchangeOAuthCode({ code })
          // 세션 저장 (액세스 토큰, 닉네임, 이메일 등)
          persistLoginSession(data.accessToken, data.nickname, data.email)
          
          // 성공 시 리디렉션 (mypage 혹은 main 중 팀의 결정에 따라 선택)
          router.replace("/mypage") 
          router.refresh()
          return
        } catch (error) {
          console.error("OAuth exchange error:", error)
          router.replace("/login?oauth=error&errorCode=OAUTH2_TOKEN_ISSUE")
          return
        }
      }

      // 3. 그 외 기본 상황은 로그인 페이지로
      router.replace("/login")
    }

    void run()
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