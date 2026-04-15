"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { ArrowLeft, Camera, Save } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  getAuthSnapshot,
  getCurrentUserProfile,
  isNicknameTaken,
  saveCurrentUserProfile,
} from "@/lib/auth-storage"

export default function EditProfilePage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const [isAuthReady, setIsAuthReady] = useState(false)
  const [error, setError] = useState("")
  const [formData, setFormData] = useState({
    name: "김개발",
    username: "kimdev",
    email: "kimdev@example.com",
    bio: "10년차 풀스택 개발자입니다. React, TypeScript, Node.js를 주로 사용합니다.",
    location: "서울, 대한민국",
    website: "https://kimdev.blog",
    github: "kimdev",
    twitter: "kimdev",
  })

  useEffect(() => {
    const auth = getAuthSnapshot()
    if (!auth.isLoggedIn) {
      router.replace("/login")
      return
    }

    const profile = getCurrentUserProfile()
    const nickname = profile?.nickname?.trim() || auth.nickname?.trim() || "김개발"
    const email = profile?.email?.trim() || auth.email?.trim() || "kimdev@example.com"
    const username =
      profile?.username?.trim() ||
      email.split("@")[0]?.trim() ||
      nickname.replace(/\s+/g, "") ||
      "kimdev"

    setFormData((prev) => ({
      ...prev,
      name: nickname,
      username,
      email,
      bio: profile?.bio ?? prev.bio,
      location: profile?.location ?? prev.location,
      website: profile?.website ?? prev.website,
      github: profile?.github ?? prev.github,
      twitter: profile?.twitter ?? prev.twitter,
    }))

    setIsAuthReady(true)
  }, [router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsLoading(true)

    try {
      const nickname = formData.name.trim()
      const email = formData.email.trim()
      const username = formData.username.trim()

      if (!nickname) {
        throw new Error("닉네임을 입력해주세요.")
      }

      if (!email) {
        throw new Error("이메일을 입력해주세요.")
      }

      const currentEmail = getAuthSnapshot().email
      if (isNicknameTaken(nickname, currentEmail)) {
        throw new Error("이미 사용 중인 닉네임입니다.")
      }

      saveCurrentUserProfile({
        email,
        nickname,
        username: username || email.split("@")[0] || nickname.replace(/\s+/g, ""),
        bio: formData.bio,
        location: formData.location,
        website: formData.website,
        github: formData.github,
        twitter: formData.twitter,
      })

      router.push("/mypage")
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "저장 중 오류가 발생했습니다.")
    } finally {
      setIsLoading(false)
    }
  }

  if (!isAuthReady) {
    return null
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center gap-4">
        <Link href="/mypage">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <h1 className="text-2xl font-bold text-foreground">프로필 수정</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-8">
        <div className="flex items-center gap-6">
          <div className="relative">
            <Avatar className="h-24 w-24 border-4 border-primary/20">
              <AvatarImage src="" alt={formData.name} />
              <AvatarFallback className="bg-primary text-2xl text-primary-foreground">
                {formData.name.slice(0, 2)}
              </AvatarFallback>
            </Avatar>
            <button
              type="button"
              className="absolute bottom-0 right-0 flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-lg hover:bg-primary/90"
            >
              <Camera className="h-4 w-4" />
            </button>
          </div>
          <div>
            <h3 className="font-semibold text-foreground">프로필 사진</h3>
            <p className="text-sm text-muted-foreground">JPG, PNG 파일을 업로드하세요</p>
          </div>
        </div>

        <div className="rounded-lg border border-border bg-card p-6">
          <h2 className="mb-6 text-lg font-semibold text-foreground">기본 정보</h2>
          <div className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="name">이름</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="bg-secondary"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="username">사용자 이름</Label>
                <Input
                  id="username"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  className="bg-secondary"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="bg-secondary"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="bio">자기소개</Label>
              <Textarea
                id="bio"
                rows={4}
                value={formData.bio}
                onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                className="resize-none bg-secondary"
                placeholder="자신을 소개해주세요"
              />
              <p className="text-xs text-muted-foreground">{formData.bio.length}/200자</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">위치</Label>
              <Input
                id="location"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                className="bg-secondary"
                placeholder="서울, 대한민국"
              />
            </div>
          </div>
        </div>

        <div className="rounded-lg border border-border bg-card p-6">
          <h2 className="mb-6 text-lg font-semibold text-foreground">링크</h2>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="website">웹사이트</Label>
              <Input
                id="website"
                type="url"
                value={formData.website}
                onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                className="bg-secondary"
                placeholder="https://example.com"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="github">GitHub</Label>
              <div className="flex items-center">
                <span className="flex h-10 items-center rounded-l-md border border-r-0 border-input bg-muted px-3 text-sm text-muted-foreground">
                  github.com/
                </span>
                <Input
                  id="github"
                  value={formData.github}
                  onChange={(e) => setFormData({ ...formData, github: e.target.value })}
                  className="rounded-l-none bg-secondary"
                  placeholder="username"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="twitter">Twitter</Label>
              <div className="flex items-center">
                <span className="flex h-10 items-center rounded-l-md border border-r-0 border-input bg-muted px-3 text-sm text-muted-foreground">
                  twitter.com/
                </span>
                <Input
                  id="twitter"
                  value={formData.twitter}
                  onChange={(e) => setFormData({ ...formData, twitter: e.target.value })}
                  className="rounded-l-none bg-secondary"
                  placeholder="username"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end gap-4">
          <Link href="/mypage">
            <Button variant="outline">취소</Button>
          </Link>
          <Button
            type="submit"
            disabled={isLoading}
            className="gap-2 bg-primary text-primary-foreground hover:bg-primary/90"
          >
            <Save className="h-4 w-4" />
            {isLoading ? "저장 중..." : "저장하기"}
          </Button>
        </div>
        {error ? <p className="text-sm text-destructive">{error}</p> : null}
      </form>
    </div>
  )
}
