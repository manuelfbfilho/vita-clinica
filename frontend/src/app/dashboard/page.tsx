"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { agendamentoApi } from "@/lib/api";
import type { Agendamento } from "@/types";

const STATUS_COLORS: Record<string, string> = {
  AGENDADO:       "bg-blue-50 text-blue-700 border-blue-200",
  CONFIRMADO:     "bg-green-50 text-green-700 border-green-200",
  CANCELADO:      "bg-red-50 text-red-600 border-red-200",
  CONCLUIDO:      "bg-slate-50 text-slate-600 border-slate-200",
  NAO_COMPARECEU: "bg-orange-50 text-orange-700 border-orange-200",
};
const STATUS_LABEL: Record<string, string> = {
  AGENDADO: "Agendado", CONFIRMADO: "Confirmado", CANCELADO: "Cancelado",
  CONCLUIDO: "Concluído", NAO_COMPARECEU: "Não Compareceu",
};

function formatDate(d: string) {
  return new Date(d + "T00:00:00").toLocaleDateString("pt-BR", {
    weekday: "short", day: "2-digit", month: "short",
  });
}

function Header({ nome, role, onLogout }: { nome: string; role: string; onLogout: () => void }) {
  const roleLabel = role === "ROLE_ADMIN" ? "Administrador" : role === "ROLE_FUNCIONARIO" ? "Funcionário" : "Paciente";
  const roleColor = role === "ROLE_ADMIN" ? "bg-purple-100 text-purple-700" : role === "ROLE_FUNCIONARIO" ? "bg-blue-100 text-blue-700" : "bg-green-100 text-green-700";
  return (
    <header className="bg-white border-b border-slate-100 shadow-sm">
      <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
            <span className="text-white text-xs font-bold">V</span>
          </div>
          <div>
            <span className="font-semibold text-slate-800 text-sm">Vita Clínica</span>
            <span className={`ml-2 text-xs px-2 py-0.5 rounded-full font-medium ${roleColor}`}>{roleLabel}</span>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-sm text-slate-600 hidden sm:block">Olá, <strong>{nome?.split(" ")[0]}</strong></span>
          <button onClick={onLogout} className="text-sm text-slate-400 hover:text-red-600 transition px-3 py-1.5 rounded-lg hover:bg-red-50">Sair</button>
        </div>
      </div>
    </header>
  );
}

function ActionCard({ emoji, title, desc, onClick, primary = false }: { emoji: string; title: string; desc: string; onClick: () => void; primary?: boolean }) {
  return (
    <button onClick={onClick} className={`rounded-xl p-5 text-left transition shadow-sm hover:shadow-md ${primary ? "bg-primary text-white hover:bg-primary-light" : "bg-white border border-slate-100 hover:border-primary/30"}`}>
      <div className="text-2xl mb-2">{emoji}</div>
      <div className={`font-semibold text-sm ${primary ? "text-white" : "text-slate-800"}`}>{title}</div>
      <div className={`text-xs mt-1 ${primary ? "text-blue-100" : "text-slate-400"}`}>{desc}</div>
    </button>
  );
}

function AgendamentoList({ agendamentos, loading, isFuncionario, onNovo }: { agendamentos: Agendamento[]; loading: boolean; isFuncionario: boolean; onNovo: () => void }) {
  return (
    <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
      <div className="px-5 py-4 border-b border-slate-50">
        <h2 className="font-semibold text-slate-800 text-sm">{isFuncionario ? "📋 Agenda de Hoje" : "📅 Próximas Consultas"}</h2>
      </div>
      {loading ? (
        <div className="flex items-center justify-center py-12 text-slate-400 text-sm">Carregando...</div>
      ) : agendamentos.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-4xl mb-3">📭</div>
          <p className="text-slate-500 text-sm">{isFuncionario ? "Sem agendamentos para hoje." : "Nenhuma consulta agendada."}</p>
          <button onClick={onNovo} className="mt-4 text-sm text-primary font-medium hover:underline">Agendar consulta</button>
        </div>
      ) : (
        <div className="divide-y divide-slate-50">
          {agendamentos.map((a) => (
            <div key={a.id} className="px-5 py-3.5 hover:bg-slate-50 transition">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-0.5 flex-wrap">
                    <span className="font-medium text-slate-800 text-sm truncate">{isFuncionario ? a.pacienteNome : a.profissionalNome}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full border flex-shrink-0 ${STATUS_COLORS[a.status]}`}>{STATUS_LABEL[a.status] || a.status}</span>
                  </div>
                  <div className="text-xs text-slate-400">
                    {a.especialidadeNome} · <span className="font-medium text-slate-600">{formatDate(a.dataConsulta)} às {a.horaConsulta?.substring(0, 5)}</span> · {a.tipoAtendimento === "PRESENCIAL" ? "Presencial" : "Virtual"}
                  </div>
                </div>
                <div className="text-xs text-slate-400 flex-shrink-0">{a.tipoConsulta}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function DashboardPage() {
  const router = useRouter();
  const { nome, role, userId, logout, isFuncionario } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { setMounted(true); }, []);

  useEffect(() => {
    if (!mounted) return;
    if (!userId) { router.push("/login"); return; }
    loadAgendamentos();
  }, [mounted, userId]);

  async function loadAgendamentos() {
    setLoading(true);
    try {
      const res = isFuncionario() ? await agendamentoApi.agendaDia() : await agendamentoApi.meus();
      const data = isFuncionario() ? res.data.data : res.data.data?.content;
      setAgendamentos(Array.isArray(data) ? data : []);
    } catch { setAgendamentos([]); }
    finally { setLoading(false); }
  }

  function handleLogout() { logout(); router.push("/login"); }

  if (!mounted) return <div className="min-h-screen bg-slate-50 flex items-center justify-center"><div className="text-slate-400 text-sm">Carregando...</div></div>;

  const isAdmin = role === "ROLE_ADMIN";
  const func = isFuncionario();

  return (
    <div className="min-h-screen bg-slate-50">
      <Header nome={nome || ""} role={role || ""} onLogout={handleLogout} />
      <main className="max-w-5xl mx-auto px-4 py-6">
        {/* Boas-vindas */}
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-slate-800">
            {func ? `Bom dia, ${nome?.split(" ")[0]}! 🏥` : `Olá, ${nome?.split(" ")[0]}! 👋`}
          </h1>
          <p className="text-slate-500 text-sm mt-1">
            {func
              ? new Date().toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" })
              : "O que você precisa hoje?"}
          </p>
        </div>

        {/* ── AÇÕES DO PACIENTE ── */}
        {!func && (
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-6">
            <ActionCard emoji="📅" title="Nova Consulta" desc="Agendar com um profissional" onClick={() => router.push("/agendamentos/novo")} primary />
            <ActionCard emoji="📋" title="Meus Agendamentos" desc="Ver histórico completo" onClick={() => router.push("/agendamentos")} />
            <ActionCard emoji="👤" title="Meu Perfil" desc="Ver e editar meus dados" onClick={() => router.push("/perfil")} />
          </div>
        )}

        {/* ── AÇÕES DO FUNCIONÁRIO / ADMIN ── */}
        {func && (
          <div className={`grid grid-cols-2 ${isAdmin ? "sm:grid-cols-4" : "sm:grid-cols-3"} gap-3 mb-6`}>
            <ActionCard emoji="📅" title="Novo Agendamento" desc="Criar consulta para paciente" onClick={() => router.push("/agendamentos/novo")} primary />
            <ActionCard emoji="📋" title="Agenda Completa" desc="Todos os agendamentos" onClick={() => router.push("/agendamentos")} />
            <ActionCard emoji="👥" title="Pacientes" desc="Buscar e gerenciar" onClick={() => router.push("/pacientes")} />
            {isAdmin && (
              <ActionCard emoji="⚙️" title="Administração" desc="Funcionários e profissionais" onClick={() => router.push("/admin")} />
            )}
          </div>
        )}

        {/* Lista de agendamentos */}
        <AgendamentoList agendamentos={agendamentos} loading={loading} isFuncionario={func} onNovo={() => router.push("/agendamentos/novo")} />
      </main>
    </div>
  );
}
