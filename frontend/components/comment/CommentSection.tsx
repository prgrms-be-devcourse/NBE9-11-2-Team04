"use client"

import { useCallback, useEffect, useState } from "react"

type CommentSectionProps = {
    postId: number
}

type CommentItem = {
    commentId: number
    postId: number
    userId: number
    nickname: string | null
    parentCommentId: number | null
    content: string
    createdAt: string
    updatedAt: string
    deleted: boolean
    replies: CommentItem[]
}

type CommentListResponse = {
    comments: CommentItem[]
}


function sortCommentsByNewest(comments: CommentItem[]): CommentItem[] {
    return [...comments]
        .map((comment) => ({
            ...comment,
            replies: sortCommentsByNewest(comment.replies ?? []),
        }))
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
}


export default function CommentSection({ postId }: CommentSectionProps) {
    const [comments, setComments] = useState<CommentItem[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [newComment, setNewComment] = useState("")
    const [submitting, setSubmitting] = useState(false)
    const [replyInputs, setReplyInputs] = useState<Record<number, string>>({})
    const [replySubmittingId, setReplySubmittingId] = useState<number | null>(null)
    const [openedReplyId, setOpenedReplyId] = useState<number | null>(null)
    const [deletingCommentId, setDeletingCommentId] = useState<number | null>(null)
    const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
    const [editInputs, setEditInputs] = useState<Record<number, string>>({})
    const [editingSubmittingId, setEditingSubmittingId] = useState<number | null>(null)

    const loadComments = useCallback(async () => {
        try {
            setLoading(true)
            setError(null)

            const response = await fetch(`http://localhost:8080/api/posts/${postId}/comments`)

            if (!response.ok) {
                throw new Error("댓글을 불러오지 못했습니다.")
            }

            const data: CommentListResponse = await response.json()
            setComments(sortCommentsByNewest(data.comments ?? []))
        } catch (err) {
            setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
        } finally {
            setLoading(false)
        }
    }, [postId])

    useEffect(() => {
        void loadComments()
    }, [loadComments])

    const handleCreateComment = async () => {
        const trimmedComment = newComment.trim()

        if (!trimmedComment) {
            return
        }

        try {
            setSubmitting(true)
            setError(null)

            const response = await fetch(`http://localhost:8080/api/posts/${postId}/comments`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    content: trimmedComment,
                }),
            })

            if (!response.ok) {
                throw new Error("댓글 작성에 실패했습니다.")
            }

            setNewComment("")
            await loadComments()
        } catch (err) {
            setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
        } finally {
            setSubmitting(false)
        }
    }

    const handleCreateReply = async (commentId: number) => {
        const content = (replyInputs[commentId] ?? "").trim()

        if (!content) {
            return
        }

        try {
            setReplySubmittingId(commentId)
            setError(null)

            const response = await fetch(`http://localhost:8080/api/comments/${commentId}/replies`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    content,
                }),
            })

            if (!response.ok) {
                throw new Error("대댓글 작성에 실패했습니다.")
            }

            setReplyInputs((prev) => ({
                ...prev,
                [commentId]: "",
            }))
            setOpenedReplyId(null)
            await loadComments()
        } catch (err) {
            setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
        } finally {
            setReplySubmittingId(null)
        }
    }

    const handleDeleteComment = async (commentId: number) => {
        try {
            setDeletingCommentId(commentId)
            setError(null)

            const response = await fetch(`http://localhost:8080/api/comments/${commentId}`, {
                method: "DELETE",
            })

            if (!response.ok) {
                throw new Error("댓글 삭제에 실패했습니다.")
            }

            if (openedReplyId === commentId) {
                setOpenedReplyId(null)
            }
            if (editingCommentId === commentId) {
                setEditingCommentId(null)
            }

            await loadComments()
        } catch (err) {
            setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
        } finally {
            setDeletingCommentId(null)
        }
    }

    const handleStartEdit = (commentId: number, currentContent: string) => {
        setEditingCommentId(commentId)
        setEditInputs((prev) => ({
            ...prev,
            [commentId]: currentContent,
        }))
    }

    const handleUpdateComment = async (commentId: number) => {
        const content = (editInputs[commentId] ?? "").trim()

        if (!content) {
            return
        }

        try {
            setEditingSubmittingId(commentId)
            setError(null)

            const response = await fetch(`http://localhost:8080/api/comments/${commentId}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    content,
                }),
            })

            if (!response.ok) {
                throw new Error("댓글 수정에 실패했습니다.")
            }

            setEditingCommentId(null)
            await loadComments()
        } catch (err) {
            setError(err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.")
        } finally {
            setEditingSubmittingId(null)
        }
    }

    return (
        <section className="rounded-xl border border-border bg-card p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-foreground">댓글</h2>

            <div className="mt-4 space-y-3 rounded-lg border border-border p-4">
                <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="댓글을 입력하세요."
                    className="min-h-[96px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none ring-offset-background placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring"
                />
                <div className="flex justify-end">
                    <button
                        type="button"
                        onClick={handleCreateComment}
                        disabled={submitting || !newComment.trim()}
                        className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50"
                    >
                        {submitting ? "작성 중..." : "댓글 작성"}
                    </button>
                </div>
            </div>

            {loading && (
                <p className="mt-4 text-sm text-muted-foreground">댓글을 불러오는 중입니다...</p>
            )}

            {error && (
                <p className="mt-4 text-sm text-destructive">{error}</p>
            )}

            {!loading && !error && comments.length === 0 && (
                <p className="mt-4 text-sm text-muted-foreground">아직 댓글이 없습니다.</p>
            )}

            <div className="mt-4 space-y-4">
                {comments.map((comment) => (
                    <div key={comment.commentId} className="rounded-lg border border-border p-4">
                        {editingCommentId === comment.commentId ? (
                            <div className="space-y-2">
        <textarea
            value={editInputs[comment.commentId] ?? ""}
            onChange={(e) =>
                setEditInputs((prev) => ({
                    ...prev,
                    [comment.commentId]: e.target.value,
                }))
            }
            className="min-h-[96px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none ring-offset-background placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring"
        />
                                <div className="flex justify-end gap-2">
                                    <button
                                        type="button"
                                        onClick={() => setEditingCommentId(null)}
                                        className="rounded-md border border-border px-3 py-2 text-sm font-medium text-foreground"
                                    >
                                        취소
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => handleUpdateComment(comment.commentId)}
                                        disabled={
                                            editingSubmittingId === comment.commentId ||
                                            !(editInputs[comment.commentId] ?? "").trim()
                                        }
                                        className="rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50"
                                    >
                                        {editingSubmittingId === comment.commentId ? "수정 중..." : "수정 완료"}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <p className="text-sm text-foreground">{comment.content}</p>
                        )}

                        <div className="mt-2 flex items-center justify-between gap-3">
                            <p className="text-xs text-muted-foreground">
                                작성자: {comment.nickname ?? `user-${comment.userId}`}
                            </p>
                            <div className="flex items-center gap-3">
                                <button
                                    type="button"
                                    onClick={() => handleStartEdit(comment.commentId, comment.content)}
                                    className="text-xs font-medium text-primary hover:underline"
                                >
                                    수정
                                </button>
                                <button
                                    type="button"
                                    onClick={() => handleDeleteComment(comment.commentId)}
                                    disabled={deletingCommentId === comment.commentId}
                                    className="text-xs font-medium text-destructive hover:underline disabled:cursor-not-allowed disabled:opacity-50"
                                >
                                    {deletingCommentId === comment.commentId ? "삭제 중..." : "삭제"}
                                </button>
                            </div>
                        </div>

                        <div className="mt-3 flex justify-end">
                            <button
                                type="button"
                                onClick={() =>
                                    setOpenedReplyId((prev) => (prev === comment.commentId ? null : comment.commentId))
                                }
                                className="text-sm font-medium text-primary hover:underline"
                            >
                                {openedReplyId === comment.commentId ? "답글 입력 닫기" : "답글 달기"}
                            </button>
                        </div>

                        {openedReplyId === comment.commentId && (
                            <div className="mt-4 ml-4 border-l-2 border-border/70 pl-4">
                                <p className="mb-2 text-xs font-medium text-muted-foreground">이 댓글에 답글 달기</p>
                                <div className="space-y-2 rounded-md bg-muted/30 p-3">
            <textarea
                value={replyInputs[comment.commentId] ?? ""}
                onChange={(e) =>
                    setReplyInputs((prev) => ({
                        ...prev,
                        [comment.commentId]: e.target.value,
                    }))
                }
                placeholder="답글을 입력하세요."
                className="min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none ring-offset-background placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring"
            />
                                    <div className="flex justify-end">
                                        <button
                                            type="button"
                                            onClick={() => handleCreateReply(comment.commentId)}
                                            disabled={
                                                replySubmittingId === comment.commentId ||
                                                !(replyInputs[comment.commentId] ?? "").trim()
                                            }
                                            className="rounded-md bg-secondary px-3 py-2 text-sm font-medium text-foreground disabled:cursor-not-allowed disabled:opacity-50"
                                        >
                                            {replySubmittingId === comment.commentId ? "작성 중..." : "답글 작성"}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}

                        {comment.replies?.length > 0 && (
                            <div className="mt-4 ml-4 space-y-2 border-l-2 border-border/70 pl-4">
                                {comment.replies.map((reply) => (
                                    <div key={reply.commentId} className="rounded-md bg-muted/50 p-3 shadow-sm">
                                        {editingCommentId === reply.commentId ? (
                                            <div className="space-y-2">
            <textarea
                value={editInputs[reply.commentId] ?? ""}
                onChange={(e) =>
                    setEditInputs((prev) => ({
                        ...prev,
                        [reply.commentId]: e.target.value,
                    }))
                }
                className="min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none ring-offset-background placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring"
            />
                                                <div className="flex justify-end gap-2">
                                                    <button
                                                        type="button"
                                                        onClick={() => setEditingCommentId(null)}
                                                        className="rounded-md border border-border px-3 py-2 text-sm font-medium text-foreground"
                                                    >
                                                        취소
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() => handleUpdateComment(reply.commentId)}
                                                        disabled={
                                                            editingSubmittingId === reply.commentId ||
                                                            !(editInputs[reply.commentId] ?? "").trim()
                                                        }
                                                        className="rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50"
                                                    >
                                                        {editingSubmittingId === reply.commentId ? "수정 중..." : "수정 완료"}
                                                    </button>
                                                </div>
                                            </div>
                                        ) : (
                                            <p className="text-sm text-foreground">{reply.content}</p>
                                        )}

                                        <div className="mt-2 flex items-center justify-between gap-3">
                                            <p className="text-xs text-muted-foreground">
                                                작성자: {reply.nickname ?? `user-${reply.userId}`}
                                            </p>
                                            <div className="flex items-center gap-3">
                                                <button
                                                    type="button"
                                                    onClick={() => handleStartEdit(reply.commentId, reply.content)}
                                                    className="text-xs font-medium text-primary hover:underline"
                                                >
                                                    수정
                                                </button>
                                                <button
                                                    type="button"
                                                    onClick={() => handleDeleteComment(reply.commentId)}
                                                    disabled={deletingCommentId === reply.commentId}
                                                    className="text-xs font-medium text-destructive hover:underline disabled:cursor-not-allowed disabled:opacity-50"
                                                >
                                                    {deletingCommentId === reply.commentId ? "삭제 중..." : "삭제"}
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </section>
    )
}