"use client"

import { useMemo } from "react"
import { useParams } from "next/navigation"
import CommentSection from "@/components/comment/CommentSection"

export default function PostDetailPage() {
    const params = useParams()
    const postId = useMemo(() => {
        const rawPostId = params?.postId
        if (Array.isArray(rawPostId)) {
            return Number(rawPostId[0])
        }
        return Number(rawPostId)
    }, [params])

    if (!postId || Number.isNaN(postId)) {
        return (
            <main className="mx-auto max-w-4xl px-4 py-10">
                <div className="rounded-xl border border-border bg-card p-6 text-center">
                    <h1 className="text-xl font-semibold text-foreground">잘못된 게시글 경로입니다.</h1>
                    <p className="mt-2 text-sm text-muted-foreground">게시글 번호를 다시 확인해주세요.</p>
                </div>
            </main>
        )
    }

    return (
        <main className="mx-auto max-w-4xl px-4 py-10">
            <section className="mb-8 rounded-xl border border-border bg-card p-6 shadow-sm">
                <h1 className="text-2xl font-bold text-foreground">게시글 상세</h1>
                <p className="mt-2 text-sm text-muted-foreground">게시글 ID: {postId}</p>
            </section>

            <CommentSection postId={postId} />
        </main>
    )
}