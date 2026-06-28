"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { authApi } from "@/lib/api";
import type { Role } from "@/types";

export default function LoginPage() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [cpf, setCpf]       = useState("");
  const [senha, setSenha]   = useState("");
  const [erro, setErro]     = useState("");
  const [loading, setLoading] = useState(false);

  function formatCpf(v: string) {
    const n = v.replace(/\D/g, "").slice(0, 11);
    return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4")
            .replace(/(\d{3})(\d{3})(\d{3})/, "$1.$2.$3")
            .replace(/(\d{3})(\d{3})/, "$1.$2")
            .replace(/(\d{3})/, "$1");
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setErro(""); setLoading(true);
    try {
      const res = await authApi.login(cpf, senha);
      const { data } = res.data;
      setAuth({ token: data.token, role: data.role as Role, userId: data.userId, nome: data.nome, cpf: data.cpf });
      router.push("/dashboard");
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem;
      setErro(msg || "CPF ou senha incorretos. Tente novamente.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-slate-100 p-4">
      <div className="w-full max-w-md">
        {/* Logo / Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary mb-4 shadow-lg">
            <svg className="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
          </div>
          <h1 className="text-2xl font-semibold text-slate-800">Vita Clínica</h1>
          <p className="text-slate-500 text-sm mt-1">Sistema de Agendamento</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-8">
          <h2 className="text-lg font-semibold text-slate-800 mb-6">Acesse sua conta</h2>

          {erro && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{erro}</div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">CPF</label>
              <input
                type="text" value={cpf} onChange={(e) => setCpf(formatCpf(e.target.value))}
                placeholder="000.000.000-00" required maxLength={14}
                className="w-full px-4 py-2.5 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Senha</label>
              <input
                type="password" value={senha} onChange={(e) => setSenha(e.target.value)}
                placeholder="Sua senha" required
                className="w-full px-4 py-2.5 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
              />
            </div>
            <button type="submit" disabled={loading}
              className="w-full py-2.5 px-4 bg-primary text-white rounded-lg font-medium text-sm hover:bg-primary-light transition disabled:opacity-60 disabled:cursor-not-allowed mt-2">
              {loading ? "Entrando..." : "Entrar"}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-slate-100 text-center">
            <p className="text-sm text-slate-500">
              Ainda não tem conta?{" "}
              <a href="/cadastro" className="text-primary font-medium hover:underline">Cadastre-se</a>
            </p>
          </div>
        </div>

        {/* Demo credentials */}
        <div className="mt-4 p-4 bg-white rounded-xl border border-slate-100 text-xs text-slate-500">
          <p className="font-medium text-slate-600 mb-2">Credenciais de demonstração:</p>
          <div className="space-y-1">
            <p>👤 Admin: <code className="bg-slate-100 px-1 rounded">000.000.000-01</code></p>
            <p>👥 Funcionário: <code className="bg-slate-100 px-1 rounded">000.000.000-02</code></p>
            <p>🧑 Paciente: <code className="bg-slate-100 px-1 rounded">111.111.111-11</code></p>
            <p>🔑 Senha: <code className="bg-slate-100 px-1 rounded">Vita@2025#</code></p>
          </div>
        </div>
      </div>
    </div>
  );
}
