const BASE_URL = "http://localhost:8080";

export type PostLikeResponse = {
  postId: number;
  liked: boolean;
  likeCount: number;
};

export type BookmarkResponse = {
  postId: number;
  bookmarked: boolean;
};

export type LikedPostResponse = {
  postId: number;
  title: string;
  authorNickname: string;
  likeCount: number;
  commentCount: number;
  createdAt: string;
};

export type BookmarkedPostResponse = {
  postId: number;
  title: string;
  authorNickname: string;
  likeCount: number;
  commentCount: number;
  createdAt: string;
};

export async function createLike(postId: number, userId: number) {
  const response = await fetch(`${BASE_URL}/posts/${postId}/likes?userId=${userId}`, {
    method: "POST",
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("좋아요 등록 실패", {
      postId,
      userId,
      status: response.status,
      statusText: response.statusText,
      body: errorText,
    });
    throw new Error(`좋아요 등록 실패: ${response.status}`);
  }

  return response.json() as Promise<PostLikeResponse>;
}

export async function cancelLike(postId: number, userId: number) {
  const response = await fetch(`${BASE_URL}/posts/${postId}/likes?userId=${userId}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("좋아요 취소 실패", {
      postId,
      userId,
      status: response.status,
      statusText: response.statusText,
      body: errorText,
    });
    throw new Error(`좋아요 취소 실패: ${response.status}`);
  }

  return response.json() as Promise<PostLikeResponse>;
}
