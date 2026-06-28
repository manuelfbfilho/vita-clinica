"use client";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Role } from "@/types";
interface AuthState {
  token: string | null; role: Role | null; userId: number | null; nome: string | null; cpf: string | null;
  setAuth: (p: { token: string; role: Role; userId: number; nome: string; cpf: string }) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
  isFuncionario: () => boolean;
}
export const useAuthStore = create<AuthState>()(persist(
  (set, get) => ({
    token: null, role: null, userId: null, nome: null, cpf: null,
    setAuth: (p) => set(p),
    logout: () => set({ token: null, role: null, userId: null, nome: null, cpf: null }),
    isAuthenticated: () => !!get().token,
    isFuncionario: () => get().role === "ROLE_FUNCIONARIO" || get().role === "ROLE_ADMIN",
  }),
  { name: "vita-auth" }
));
