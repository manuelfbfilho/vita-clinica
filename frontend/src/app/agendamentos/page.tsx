"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { agendamentoApi } from "@/lib/api";
import type { Agendamento, StatusAgendamento } from "@/types";

const STATUS_COLORS: Record<string, string> = {
  AGENDADO: "bg-blue-50 text-blue-700 border-blue-200",
  CONFIRMADO: "bg-green-50 text-green-700 border-green-200",
  CANCELADO: "bg-red-50 text-red-600 border-red-200",
  CONCLUIDO: "bg-slate-50 text-slate-600 border-slate-200",
  NAO_COMPARECEU: "bg-orange-50 text-orange-700 border-orange-200",
};
const STATUS_LABEL: Record<string, string> = {
  AGENDADO: "Agendado",
  CONFIRMADO: "Confirmado",
  CANCELADO: "Cancelado",
  CONCLUIDO: "Concluído",
  NAO_COMPARECEU: "Não Compareceu",
};

export default function AgendamentosPage() {
  const router = useRouter();
  const { userId, isFuncionario, mounted: _m } = { ...useAuthStore(), mounted: false };
  const [mounted, setMounted] = useState(false);
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancelandoId, setCancelandoId] = useState<number | null>(null);
  const [motivo, setMotivo] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filtroStatus, setFiltroStatus] = useState<StatusAgendamento | "">("");

  useEffect(() => { setMounted(true); }, []);

  useEffect(() => {
    if (!mounted) return;
    if (!userId) { router.push("/login"); return; }
    carregar();
  }, [mounted, userId, page, filtroStatus]);

  async function carregar() {
    setLoading(true);
    try {
      let res;
      if (isFuncionario()) {
        res = await agendamentoApi.listar({ page, size: 10, status: filtroStatus || undefined });
        const d = res.data.data;
        setAgendamentos(d?.content || []);
        setTotalPages(d?.totalPages || 1);
      } else {
        res = await agendamentoApi.meus(page);
        const d = res.data.data;
        setAgendamentos(d?.content || []);
        setTotalPages(d?.totalPages || 1);
      }
    } catch {
      setAgendamentos([]);
    } finally {
      setLoading(false);
    }
  }

  async function cancelar(id: number) {
    if (!motivo || motivo.length < 10) return;
    try {
      await agendamentoApi.cancelar(id, motivo);
      setCancelandoId(null);
      setMotivo("");
      carregar();
    } catch {
      alert("Erro ao cancelar. Tente novamente.");
    }
  }

  function formatDate(d: string) {
    return new Date(d + "T00:00:00").toLocaleDateString("pt-BR", { weekday: "short", day: "2-digit", month: "short", year: "numeric" });
  }

  if (!mounted) return <div className="min-h-screen bg-slate-50 flex items-center justify-center"><div className="text-slate-400 text-sm">Carregando...</div></div>;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push("/dashboard")} className="text-slate-400 hover:text-slate-700">←</button>
          <div>
            <h1 className="font-semibold text-slate-800 text-sm">{isFuncionario() ? "Todos os Agendamentos" : "Meus Agendamentos"}</h1>
            <p className="text-xs text-slate-400">Histórico completo</p>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {/* Filtros */}
        <div className="flex items-center gap-3 mb-4">
          <select
            value={filtroStatus}
            onChange={e => { setFiltroStatus(e.target.value as StatusAgendamento | ""); setPage(0); }}
            className="text-sm border border-slate-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary/20"
          >
            <option value="">Todos os status</option>
            <option value="AGENDADO">Agendado</option>
            <option value="CONFIRMADO">Confirmado</option>
            <option value="CANCELADO">Cancelado</option>
            <option value="CONCLUIDO">Concluído</option>
          </select>
          <button onClick={() => router.push("/agendamentos/novo")}
            className="ml-auto px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light transition">
            + Novo Agendamento
          </button>
        </div>

        {/* Lista */}
        <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-16 text-slate-400 text-sm">Carregando...</div>
          ) : agendamentos.length === 0 ? (
            <div className="text-center py-16">
              <div className="text-4xl mb-3">📭</div>
              <p className="text-slate-500 text-sm">Nenhum agendamento encontrado.</p>
              <button onClick={() => router.push("/agendamentos/novo")}
                className="mt-4 text-sm text-primary font-medium hover:underline">
                Agendar uma consulta
              </button>
            </div>
          ) : (
            <div className="divide-y divide-slate-50">
              {agendamentos.map((a) => (
                <div key={a.id} className="px-5 py-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <span className="font-medium text-slate-800 text-sm">
                          {isFuncionario() ? a.pacienteNome : a.profissionalNome}
                        </span>
                        <span className={`text-xs px-2 py-0.5 rounded-full border ${STATUS_COLORS[a.status]}`}>
                          {STATUS_LABEL[a.status] || a.status}
                        </span>
                      </div>
                      <div className="text-xs text-slate-500">
                        {a.especialidadeNome} · {formatDate(a.dataConsulta)} às {a.horaConsulta?.substring(0, 5)}
                        {" · "}{a.tipoAtendimento === "PRESENCIAL" ? "Presencial" : "Virtual"}
                      </div>
                      {a.motivoCancelamento && (
                        <p className="text-xs text-red-600 mt-1">Motivo: {a.motivoCancelamento}</p>
                      )}
                    </div>

                    {/* Ação cancelar */}
                    {(a.status === "AGENDADO" || a.status === "CONFIRMADO") && (
                      <div className="shrink-0">
                        {cancelandoId === a.id ? (
                          <div className="text-right">
                            <textarea
                              value={motivo}
                              onChange={e => setMotivo(e.target.value)}
                              placeholder="Motivo do cancelamento (mín. 10 chars)"
                              rows={2}
                              className="w-48 text-xs border border-slate-200 rounded-lg px-2 py-1.5 resize-none mb-1.5 focus:outline-none"
                            />
                            <div className="flex gap-2 justify-end">
                              <button onClick={() => { setCancelandoId(null); setMotivo(""); }}
                                className="text-xs text-slate-500 hover:text-slate-700">
                                Voltar
                              </button>
                              <button
                                onClick={() => cancelar(a.id)}
                                disabled={motivo.length < 10}
                                className="text-xs px-2 py-1 bg-red-600 text-white rounded-lg disabled:opacity-40">
                                Confirmar
                              </button>
                            </div>
                          </div>
                        ) : (
                          <button
                            onClick={() => { setCancelandoId(a.id); setMotivo(""); }}
                            className="text-xs text-red-600 hover:underline">
                            Cancelar
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Paginação */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-3 mt-4">
            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
              className="px-3 py-1.5 text-sm border border-slate-200 rounded-lg disabled:opacity-40 hover:bg-slate-50">
              ← Anterior
            </button>
            <span className="text-sm text-slate-500">Página {page + 1} de {totalPages}</span>
            <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
              className="px-3 py-1.5 text-sm border border-slate-200 rounded-lg disabled:opacity-40 hover:bg-slate-50">
              Próxima →
            </button>
          </div>
        )}
      </main>
    </div>
  );
}
