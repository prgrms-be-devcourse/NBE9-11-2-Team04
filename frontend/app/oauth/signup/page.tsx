"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Code2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { completeOAuthSignup } from "@/lib/interaction";

export default function OAuthSignupPage() {
  const router = useRouter();
  const [nickname, setNickname] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");

    const trimmed = nickname.trim();
    if (trimmed.length < 2 || trimmed.length > 50) {
      setError("닉네임은 2자 이상 50자 이하로 입력해주세요.");
      return;
    }

    setIsLoading(true);
    try {
      const data = await completeOAuthSignup({ nickname: trimmed });

      if (typeof window !== "undefined") {
        const rawUserProfile = localStorage.getItem("userProfile");
        const existingUserProfile = rawUserProfile ? JSON.parse(rawUserProfile) : {};

        localStorage.setItem(
          "userProfile",
          JSON.stringify({
            ...existingUserProfile,
            email: data.email,
            nickname: data.nickname,
          })
        );
      }

      router.replace("/");
      router.refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : "닉네임 저장에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-8rem)] items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <Link href="/" className="inline-flex items-center gap-2">
            <Code2 className="h-8 w-8 text-primary" />
            <span className="text-2xl font-bold text-foreground">DevHub</span>
          </Link>
          <p className="mt-2 text-muted-foreground">
            GitHub 가입을 완료하려면 닉네임을 설정해주세요.
          </p>
        </div>

        <div className="rounded-lg border border-border bg-card p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="nickname">닉네임</Label>
              <Input
                id="nickname"
                type="text"
                placeholder="사용할 닉네임"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                required
                minLength={2}
                maxLength={50}
                className="bg-secondary"
              />
            </div>

            <Button
              type="submit"
              className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
              disabled={isLoading}
            >
              {isLoading ? "저장 중..." : "닉네임 저장하고 시작하기"}
            </Button>

            {error ? <p className="text-sm text-destructive">{error}</p> : null}
          </form>
        </div>
      </div>
    </div>
  );
}