import { apiFetch } from "./api";

type SuccessResponse<T> = {
  code: string;
  message: string;
  timestamp: string;
  data: T;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginData = {
  userId: number;
  email: string;
  nickname: string;
  role: string;
  status: string;
  accessToken: string;
};

export async function login(body: LoginRequest): Promise<LoginData> {
  const res = await apiFetch<SuccessResponse<LoginData>>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(body),
  });

  return res.data;
}
