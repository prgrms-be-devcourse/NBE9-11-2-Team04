import { apiFetch } from "./api"

type SuccessResponse<T> = {
  code: string
  message: string
  timestamp: string
  data: T
}

export type LoginRequest = {
  email: string
  password: string
}

export type LoginData = {
  userId: number
  email: string
  nickname: string
  role: string
  status: string
  accessToken: string
  refreshToken: string
}

export type SignUpRequest = {
  email: string
  password: string
  nickname: string
}

export type SignUpData = {
  userId: number
  email: string
  nickname: string
  role: string
  status: string
}

export async function login(body: LoginRequest): Promise<LoginData> {
  const res = await apiFetch<SuccessResponse<LoginData>>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(body),
  })

  return res.data
}

export async function signup(body: SignUpRequest): Promise<SignUpData> {
  const res = await apiFetch<SuccessResponse<SignUpData>>("/api/auth/signup", {
    method: "POST",
    body: JSON.stringify(body),
  })

  return res.data
}