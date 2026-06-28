"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { agendamentoApi } from "@/lib/api";
import type { Agendamento } from "@/types";

const STATUS_COLORS: Record<string, string> = {
  AGENDADO: "bg-blue-50 text-blue-700 border-blue-200",
  CONFIRMADO: "bg-green-50 text-green-700 border-green-200",
  CANCELADO: "bg-red-50 text-red-600 border-red-200",
  CONCLUIDO: "bg-slate-50 text-slate-600 border-slate-200",
  NAO_COMPARECEU: "bg-orange-50 text-orange-700 border-orange-200",
};
const STATUS_LABEL: Record<string, string> = {
  AGENDADO: "Agendado", CONFIRMADO: "Confirmado", CANCELADO: "Cancelado",
  CONCLUIDO: "Concluído", NAO_COMPARECEU: "Não Compareceu",
};

export default function DashboardPage() {
  const router = useRouter();
  const { nome, role, userId, logout, isFuncionario } = useAuthStore();
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId) { router.push("/login"); return; }
    loadAgendamentos();
  }, [userId]);

  async function loadAgendamentos() {
    try {
      const res = isFuncionario()
        ? await agendamentoApi.agendaDia()
        : await agendamentoApi.meus();
      const data = isFuncionario() ? res.data.data : res.data.data?.content;
      setAgendamentos(Array.isArray(data) ? data : []);
    } catch { setAgendamentos([]); }
    finally { setLoading(false); }
  }

  function formatDate(d: string) {
    return new Date(d + "T00:00:00").toLocaleDateString("pt-BR");
  }

  const titulo = isFuncionario() ? "Agenda de Hoje" : "Meus Agendamentos";

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-5xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white text-xs font-bold">V</span>
            </div>
            <div>
              <span className="font-semibold text-slate-800 text-sm">Vita Clínica</span>
              <span className="ml-2 text-xs text-slate-400 hidden sm:inline">
                {role === "ROLE_ADMIN" ? "Administrador" : role === "ROLE_FUNCIONARIO" ? "Funcionário" : "Paciente"}
              </span>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-slate-600 hidden sm:block">Olá, {nome?.split(" ")[0]}</span>
            <button onClick={() => { logout(); router.push("/login"); }}
              className="text-sm text-slate-500 hover:text-red-600 transition px-3 py-1.5 rounded-lg hover:bg-red-50">
              Sair
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-8">
        {/* Quick actions */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
          <button onClick={() => router.push("/agendamentos/novo")}
            className="bg-primary text-white rounded-xl p-5 text-left hover:bg-primary-light transition shadow-sm">
            <div className="text-xl mb-2">📅</div>
            <div className="font-medium text-sm">Novo Agendamento</div>
            <div className="text-blue-200 text-xs mt-1">Marcar uma consulta</div>
          </button>
          <button onClick={() => router.push("/agendamentos")}
            className="bg-white border border-slate-100 rounded-xl p-5 text-left hover:border-primary/30 hover:shadow-sm transition">
            <div className="text-xl mb-2">📋</div>
            <div className="font-medium text-sm text-slate-800">Ver Agendamentos</div>
            <div className="text-slate-400 text-xs mt-1">Histórico completo</div>
          </button>
          {!isFuncionario() && (
            <button onClick={() => router.push("/perfil")}
              className="bg-white border border-slate-100 rounded-xl p-5 text-left hover:border-primary/30 hover:shadow-sm transition">
              <div className="text-xl mb-2">👤</div>
              <div className="font-medium text-sm text-slate-800">Meu Perfil</div>
              <div className="text-slate-400 text-xs mt-1">Dados pessoais</div>
            </button>
          )}
          {isFuncionario() && (
            <button onClick={() => router.push("/pacientes")}
              className="bg-white border border-slate-100 rounded-xl p-5 text-left hover:border-primary/30 hover:shadow-sm transition">
              <div className="text-xl mb-2">👥</div>
              <div className="font-medium text-sm text-slate-800">Pacientes</div>
              <div className="text-slate-400 text-xs mt-1">Gerenciar cadastros</div>
            </button>
          )}
        </div>

        {/* Agendamentos */}
        <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 className="font-semibold text-slate-800">{titulo}</h2>
            <button onClick={loadAgendamentos} className="text-xs text-primary hover:underline">Atualizar</button>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-16 text-slate-400">Carregando...</div>
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
                <div key={a.id} className="px-6 py-4 hover:bg-slate-50 transition">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-medium text-slate-800 text-sm truncate">
                          {isFuncionario() ? a.pacienteNome : a.profissionalNome}
                        </span>
                        <span className={`text-xs px-2 py-0.5 rounded-full border ${STATUS_COLORS[a.status] || ""}`}>
                          {STATUS_LABEL[a.status] || a.status}
                        </span>
                      </div>
                      <div className="text-xs text-slate-500">
                        {a.especialidadeNome} · {formatDate(a.dataConsulta)} às {a.horaConsulta?.substring(0, 5)}
                        {" · "}{a.tipoAtendimento === "PRESENCIAL" ? "Presencial" : "Virtual"}
                      </div>
                    </div>
                    <div className="text-right shrink-0">
                      <div className="text-xs text-slate-400">{a.tipoConsulta}</div>
                    </div>
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
