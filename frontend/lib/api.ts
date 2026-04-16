const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

type ErrorResponse = {
  code?: string;
  message?: string;
  validation?: Record<string, string>;
};

type RequestOptions = RequestInit & {
  auth?: boolean;
  token?: string | null;
};

function getStoredAccessToken() {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("accessToken");
}

export async function apiFetch<T>(
  path: string,
  options: RequestOptions = {}
): Promise<T> {
  if (!API_BASE_URL) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not set");
  }

  const { auth = false, token, headers, ...rest } = options;

  const accessToken = token ?? getStoredAccessToken();

  const finalHeaders: HeadersInit = {
    "Content-Type": "application/json",
    ...headers,
  };

  if (auth && accessToken) {
    finalHeaders.Authorization = `Bearer ${accessToken}`;
  }

  let res: Response;

  try {
    console.log("REQUEST URL =", `${API_BASE_URL}${path}`);

    res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...(init?.headers || {}),
      },
      // JWT를 body로 받는 구조라면 credentials 불필요 (CORS 이슈 줄임)
      // credentials: "include",
    });
  } catch {
    throw new Error("백엔드 서버 연결 실패 (CORS/서버실행/주소 확인)");
  }

  const text = await res.text();
  const parsed = text ? (JSON.parse(text) as T | ErrorResponse) : null;

  if (res.status === 401) {
    throw new Error("UNAUTHORIZED");
  }

  if (!res.ok) {
    const err = parsed as ErrorResponse | null;
    const validationMessage = err?.validation
      ? Object.values(err.validation)[0]
      : undefined;

    throw new Error(
      validationMessage || err?.message || `API request failed (${res.status})`
    );
  }

  return parsed as T;
}