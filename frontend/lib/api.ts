const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

type ErrorResponse = {
  code?: string;
  message?: string;
  validation?: Record<string, string>;
};

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  if (!API_BASE_URL) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not set");
  }

  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...(init?.headers || {}),
      },
      credentials: "include",
    });
  } catch {
    throw new Error("백엔드 서버 연결 실패 (CORS/서버실행/주소 확인)");
  }

  const text = await res.text();
  const parsed = text ? (JSON.parse(text) as ErrorResponse | T) : null;

  if (!res.ok) {
    const err = parsed as ErrorResponse | null;
    const validationMessage = err?.validation ? Object.values(err.validation)[0] : undefined;
    throw new Error(validationMessage || err?.message || `API request failed (${res.status})`);
  }

  return parsed as T;
}
