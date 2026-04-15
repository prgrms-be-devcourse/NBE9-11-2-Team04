export const AUTH_TOKEN_KEY = "accessToken";
export const AUTH_NICKNAME_KEY = "nickname";
export const AUTH_EMAIL_KEY = "email";
export const AUTH_CHANGED_EVENT = "auth-changed";
export const AUTH_PROFILES_KEY = "userProfiles";

export type AuthSnapshot = {
  token: string | null;
  nickname: string | null;
  email: string | null;
  isLoggedIn: boolean;
};

export type UserProfile = {
  email: string;
  nickname: string;
  username: string;
  bio: string;
  location: string;
  website: string;
  github: string;
  twitter: string;
};

type UserProfileMap = Record<string, UserProfile>;

export function getAuthSnapshot(): AuthSnapshot {
  if (typeof window === "undefined") {
    return { token: null, nickname: null, email: null, isLoggedIn: false };
  }

  const token = localStorage.getItem(AUTH_TOKEN_KEY);
  const nickname = localStorage.getItem(AUTH_NICKNAME_KEY);
  const email = localStorage.getItem(AUTH_EMAIL_KEY);

  return {
    token,
    nickname,
    email,
    isLoggedIn: Boolean(token),
  };
}

function notifyAuthChanged() {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(new CustomEvent(AUTH_CHANGED_EVENT));
}

function readProfileMap(): UserProfileMap {
  if (typeof window === "undefined") {
    return {};
  }

  const raw = localStorage.getItem(AUTH_PROFILES_KEY);
  if (!raw) {
    return {};
  }

  try {
    return JSON.parse(raw) as UserProfileMap;
  } catch {
    return {};
  }
}

function writeProfileMap(profileMap: UserProfileMap) {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.setItem(AUTH_PROFILES_KEY, JSON.stringify(profileMap));
}

export function persistLoginSession(
  accessToken: string,
  nickname?: string | null,
  email?: string | null
) {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.setItem(AUTH_TOKEN_KEY, accessToken);

  if (nickname && nickname.trim().length > 0) {
    localStorage.setItem(AUTH_NICKNAME_KEY, nickname);
  } else {
    localStorage.removeItem(AUTH_NICKNAME_KEY);
  }

  if (email && email.trim().length > 0) {
    localStorage.setItem(AUTH_EMAIL_KEY, email);
  } else {
    localStorage.removeItem(AUTH_EMAIL_KEY);
  }

  const normalizedEmail = email?.trim();
  const normalizedNickname = nickname?.trim();
  if (normalizedEmail && normalizedNickname) {
    const profileMap = readProfileMap();
    const prev = profileMap[normalizedEmail];
    const username = normalizedEmail.split("@")[0] ?? normalizedNickname;

    profileMap[normalizedEmail] = {
      email: normalizedEmail,
      nickname: normalizedNickname,
      username: prev?.username || username,
      bio: prev?.bio || "",
      location: prev?.location || "",
      website: prev?.website || "",
      github: prev?.github || "",
      twitter: prev?.twitter || "",
    };
    writeProfileMap(profileMap);
  }

  notifyAuthChanged();
}

export function getCurrentUserProfile(): UserProfile | null {
  const auth = getAuthSnapshot();
  const email = auth.email?.trim();
  if (!email) {
    return null;
  }

  return readProfileMap()[email] ?? null;
}

export function isNicknameTaken(nickname: string, currentEmail?: string | null): boolean {
  const target = nickname.trim().toLowerCase();
  if (!target) {
    return false;
  }

  const normalizedCurrentEmail = currentEmail?.trim().toLowerCase();
  const profileMap = readProfileMap();

  return Object.values(profileMap).some((profile) => {
    const email = profile.email.trim().toLowerCase();
    if (normalizedCurrentEmail && email === normalizedCurrentEmail) {
      return false;
    }

    return profile.nickname.trim().toLowerCase() === target;
  });
}

export function saveCurrentUserProfile(nextProfile: UserProfile): void {
  if (typeof window === "undefined") {
    return;
  }

  const auth = getAuthSnapshot();
  const prevEmail = auth.email?.trim();
  const nextEmail = nextProfile.email.trim();
  const profileMap = readProfileMap();

  if (prevEmail && prevEmail !== nextEmail) {
    delete profileMap[prevEmail];
  }

  profileMap[nextEmail] = {
    ...nextProfile,
    email: nextEmail,
    nickname: nextProfile.nickname.trim(),
    username: nextProfile.username.trim(),
    bio: nextProfile.bio,
    location: nextProfile.location,
    website: nextProfile.website,
    github: nextProfile.github,
    twitter: nextProfile.twitter,
  };
  writeProfileMap(profileMap);

  if (auth.token) {
    persistLoginSession(auth.token, nextProfile.nickname, nextEmail);
  } else {
    notifyAuthChanged();
  }
}

export function clearLoginSession() {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_NICKNAME_KEY);
  localStorage.removeItem(AUTH_EMAIL_KEY);
  notifyAuthChanged();
}
