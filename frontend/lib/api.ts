import { getAccessToken, sanitizeAccessToken } from "./auth-storage"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL

type ErrorResponse = {
  code?: string
  message?: string
  validation?: Record<string, string>
}

type RequestOptions = RequestInit & {
  auth?: boolean
  token?: string | null
  retryOnUnauthorized?: boolean
}

function isSafeMethod(method?: string) {
  const normalized = (method ?? "GET").toUpperCase()
  return normalized === "GET" || normalized === "HEAD" || normalized === "OPTIONS"
}

function buildHeaders(
  rest: Omit<RequestInit, "headers">,
  headers: HeadersInit | undefined,
  auth: boolean,
  accessToken: string | null
): Headers {
  const finalHeaders = new Headers()

  if (!(rest.body instanceof FormData)) {
    finalHeaders.set("Content-Type", "application/json")
  }

  if (headers) {
    const incoming = new Headers(headers)
    incoming.forEach((value, key) => {
      finalHeaders.set(key, value)
    })
  }

  if (auth && accessToken) {
    finalHeaders.set("Authorization", `Bearer ${accessToken}`)
  }

  return finalHeaders
}

async function performFetch(
  path: string,
  requestInit: RequestInit
): Promise<Response> {
  return fetch(`${API_BASE_URL}${path}`, {
    ...requestInit,
    credentials: "include",
  })
}

export async function apiFetch<T>(
  path: string,
  options: RequestOptions = {}
): Promise<T> {
  if (!API_BASE_URL) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not set")
  }

  const {
    auth = false,
    token,
    headers,
    retryOnUnauthorized = true,
    ...rest
  } = options

  let accessToken =
    token !== undefined ? sanitizeAccessToken(token) : getAccessToken()

  let finalHeaders = buildHeaders(rest, headers, auth, accessToken)

  let res: Response
  try {
    res = await performFetch(path, {
      ...rest,
      headers: finalHeaders,
    })
  } catch (error) {
    console.error("FETCH ERROR =", error)
    throw new Error("백엔드 서버 연결 실패 (CORS/서버실행/주소 확인)")
  }

  // 401 1회 완화 처리:
  // - 인증 요청(auth=true)인 경우만
  // - 부작용 없는 메서드(GET/HEAD/OPTIONS)인 경우만 재시도
  if (
    res.status === 401 &&
    auth &&
    retryOnUnauthorized &&
    isSafeMethod(rest.method)
  ) {
    accessToken = getAccessToken()
    finalHeaders = buildHeaders(rest, headers, auth, accessToken)

    try {
      const retryRes = await performFetch(path, {
        ...rest,
        headers: finalHeaders,
      })
      res = retryRes
    } catch (error) {
      console.error("RETRY FETCH ERROR =", error)
      throw new Error("백엔드 서버 연결 실패 (재시도)")
    }
  }

  const text = await res.text()

  let parsed: T | ErrorResponse | null = null
  try {
    parsed = text ? (JSON.parse(text) as T | ErrorResponse) : null
  } catch {
    parsed = null
  }

  if (res.status === 401) {
    throw new Error("UNAUTHORIZED")
  }

  if (!res.ok) {
    const err = parsed as ErrorResponse | null
    const validationMessage = err?.validation
      ? Object.values(err.validation)[0]
      : undefined

    throw new Error(
      validationMessage || err?.message || `API request failed (${res.status})`
    )
  }

  return parsed as T
}
