"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import api from "@/lib/api";

interface Paciente { id: number; nomeCompleto: string; cpf: string; email: string; telefone: string; ativo: boolean; }

export default function PacientesPage() {
  const router = useRouter();
  const { isFuncionario, userId } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [busca, setBusca] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => { setMounted(true); }, []);
  useEffect(() => {
    if (!mounted) return;
    if (!userId || !isFuncionario()) { router.push("/dashboard"); return; }
    carregar();
  }, [mounted, userId]);

  async function carregar(termo = "") {
    setLoading(true);
    try {
      const res = await api.get("/pacientes", { params: { busca: termo || undefined, page: 0, size: 20 } });
      setPacientes(res.data.data?.content || []);
    } catch { setPacientes([]); }
    finally { setLoading(false); }
  }

  function handleBusca(v: string) { setBusca(v); if (v.length === 0 || v.length >= 3) carregar(v); }

  if (!mounted) return <div className="min-h-screen bg-slate-50 flex items-center justify-center"><div className="text-slate-400 text-sm">Carregando...</div></div>;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push("/dashboard")} className="text-slate-400 hover:text-slate-700">←</button>
          <h1 className="font-semibold text-slate-800 text-sm">Pacientes</h1>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-4 py-6">
        <div className="flex gap-3 mb-4">
          <input value={busca} onChange={e => handleBusca(e.target.value)}
            placeholder="Buscar por nome ou CPF..."
            className="flex-1 px-4 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
          <button onClick={() => router.push("/agendamentos/novo")}
            className="px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light transition">
            + Novo Agendamento
          </button>
        </div>
        <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-16 text-slate-400 text-sm">Carregando...</div>
          ) : pacientes.length === 0 ? (
            <div className="text-center py-16">
              <div className="text-4xl mb-3">👥</div>
              <p className="text-slate-500 text-sm">{busca ? `Nenhum paciente encontrado para "${busca}".` : "Nenhum paciente cadastrado."}</p>
            </div>
          ) : (
            <div className="divide-y divide-slate-50">
              {pacientes.map(p => (
                <div key={p.id} className="px-5 py-3.5 flex items-center justify-between">
                  <div>
                    <div className="font-medium text-slate-800 text-sm">{p.nomeCompleto}</div>
                    <div className="text-xs text-slate-400">{p.cpf} · {p.email}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    {!p.ativo && <span className="text-xs text-red-500 bg-red-50 px-2 py-0.5 rounded-full">Inativo</span>}
                    <button onClick={() => router.push(`/agendamentos/novo`)}
                      className="text-xs text-primary hover:underline">Agendar</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
