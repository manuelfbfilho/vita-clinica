"use client";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Role } from "@/types";

interface AuthState {
  token: string | null;
  role: Role | null;
  userId: number | null;
  nome: string | null;
  cpf: string | null;
  setAuth: (p: { token: string; role: Role; userId: number; nome: string; cpf: string }) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
  isFuncionario: () => boolean;
  isAdmin: () => boolean;
}

function setCookie(name: string, value: string, days = 1) {
  if (typeof document === "undefined") return;
  const expires = new Date(Date.now() + days * 864e5).toUTCString();
  document.cookie = `${name}=${value}; path=/; expires=${expires}; SameSite=Lax`;
}

function deleteCookie(name: string) {
  if (typeof document === "undefined") return;
  document.cookie = `${name}=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT`;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      role: null,
      userId: null,
      nome: null,
      cpf: null,

      setAuth: (p) => {
        setCookie("vita-token", p.token, 1); // 1 dia
        set(p);
      },

      logout: () => {
        deleteCookie("vita-token");
        set({ token: null, role: null, userId: null, nome: null, cpf: null });
      },

      isAuthenticated: () => !!get().token,
      isFuncionario: () =>
        get().role === "ROLE_FUNCIONARIO" || get().role === "ROLE_ADMIN",
      isAdmin: () => get().role === "ROLE_ADMIN",
    }),
    { name: "vita-auth" }
  )
);
