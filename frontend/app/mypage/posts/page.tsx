"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { getAuthSnapshot } from "@/lib/auth-storage";

type SuccessResponse<T> = {
  code: string;
  message: string;
  timestamp: string;
  data: T;
};

type MyPostResponse = {
  postId: number;
  title: string;
  likeCount: number;
  commentCount: number;
  createdAt: string;
};

export default function MyPostsPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<MyPostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const auth = getAuthSnapshot();

    if (!auth.token) {
      router.replace("/login");
      return;
    }

    const fetchPosts = async () => {
      try {
        setLoading(true);
        setError("");

        const res = await apiFetch<SuccessResponse<MyPostResponse[]>>(
          "/users/me/posts",
          {
            method: "GET",
            auth: true,
          }
        );

        setPosts(res.data ?? []);
      } catch (err) {
        if (err instanceof Error && err.message === "UNAUTHORIZED") {
          router.replace("/login");
          return;
        }

        console.error(err);
        setError("내가 쓴 글을 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [router]);

  return (
    <main className="mx-auto max-w-4xl px-4 py-10">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">내가 쓴 글</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            내가 작성한 게시글 목록입니다.
          </p>
        </div>

        <Link href="/mypage" className="rounded-lg border px-4 py-2 text-sm">
          마이페이지로
        </Link>
      </div>

      {loading && <div className="rounded-2xl border p-6">로딩 중...</div>}

      {!loading && error && (
        <div className="rounded-2xl border p-6">
          <p className="text-sm text-red-500">{error}</p>
        </div>
      )}

      {!loading && !error && posts.length === 0 && (
        <div className="rounded-2xl border p-6">작성한 글이 없습니다.</div>
      )}

      <div className="space-y-4">
        {!loading &&
          !error &&
          posts.map((post) => (
            <div key={post.postId} className="rounded-2xl border p-5">
              <div className="mb-3 flex items-center justify-between gap-4">
                <h2 className="text-lg font-semibold">{post.title}</h2>
                <span className="shrink-0 text-xs text-muted-foreground">
                  {post.createdAt}
                </span>
              </div>

              <div className="flex gap-4 text-sm text-muted-foreground">
                <span>좋아요 {post.likeCount}</span>
                <span>댓글 {post.commentCount}</span>
              </div>
            </div>
          ))}
      </div>
    </main>
  );
}