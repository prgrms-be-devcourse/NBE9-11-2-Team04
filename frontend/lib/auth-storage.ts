export const AUTH_TOKEN_KEY = "accessToken";
export const AUTH_NICKNAME_KEY = "nickname";
export const AUTH_CHANGED_EVENT = "auth-changed";

export type AuthSnapshot = {
  token: string | null;
  nickname: string | null;
  isLoggedIn: boolean;
};

export function getAuthSnapshot(): AuthSnapshot {
  if (typeof window === "undefined") {
    return { token: null, nickname: null, isLoggedIn: false };
  }

  const token = localStorage.getItem(AUTH_TOKEN_KEY);
  const nickname = localStorage.getItem(AUTH_NICKNAME_KEY);

  return {
    token,
    nickname,
    isLoggedIn: Boolean(token),
  };
}

function notifyAuthChanged() {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(new CustomEvent(AUTH_CHANGED_EVENT));
}

export function persistLoginSession(accessToken: string, nickname?: string | null) {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.setItem(AUTH_TOKEN_KEY, accessToken);

  if (nickname && nickname.trim().length > 0) {
    localStorage.setItem(AUTH_NICKNAME_KEY, nickname);
  } else {
    localStorage.removeItem(AUTH_NICKNAME_KEY);
  }

  notifyAuthChanged();
}

export function clearLoginSession() {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_NICKNAME_KEY);
  notifyAuthChanged();
}
