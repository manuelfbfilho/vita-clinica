"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { agendamentoApi } from "@/lib/api";
import type { Agendamento } from "@/types";
import PanelNovoAgendamento from "@/components/panels/PanelNovoAgendamento";
import PanelAgendamentos from "@/components/panels/PanelAgendamentos";
import PanelPerfil from "@/components/panels/PanelPerfil";
import PanelPacientes from "@/components/panels/PanelPacientes";
import PanelProfissionais from "@/components/panels/PanelProfissionais";

type View = "inicio" | "novo" | "agendamentos" | "perfil" | "pacientes" | "profissionais";

interface MenuItem { id: View; emoji: string; title: string; desc: string; }

const MENU_PACIENTE: MenuItem[] = [
  { id: "novo",          emoji: "📅", title: "Nova Consulta",      desc: "Agendar com um profissional" },
  { id: "agendamentos",  emoji: "📋", title: "Meus Agendamentos",  desc: "Ver e cancelar consultas" },
  { id: "perfil",        emoji: "👤", title: "Meu Perfil",         desc: "Atualizar meus dados" },
];

const MENU_FUNCIONARIO: MenuItem[] = [
  { id: "novo",          emoji: "📅", title: "Novo Agendamento",   desc: "Criar consulta para paciente" },
  { id: "agendamentos",  emoji: "📋", title: "Agendamentos",       desc: "Ver e cancelar consultas" },
  { id: "pacientes",     emoji: "👥", title: "Pacientes",          desc: "Buscar e cadastrar" },
];

const MENU_ADMIN: MenuItem[] = [
  ...MENU_FUNCIONARIO,
  { id: "profissionais", emoji: "👨‍⚕️", title: "Profissionais",   desc: "Cadastrar médicos" },
];

export default function DashboardPage() {
  const router = useRouter();
  const { nome, role, userId, logout, isFuncionario, isAdmin } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [view, setView] = useState<View>("inicio");
  const [proximos, setProximos] = useState<Agendamento[]>([]);
  const [loadingAg, setLoadingAg] = useState(true);

  useEffect(() => { setMounted(true); }, []);
  useEffect(() => {
    if (!mounted) return;
    if (!userId) { router.push("/login"); return; }
    loadProximos();
  }, [mounted, userId]);

  async function loadProximos() {
    setLoadingAg(true);
    try {
      const res = isFuncionario()
        ? await agendamentoApi.proximos()
        : await agendamentoApi.meus();
      const data = isFuncionario() ? res.data.data : res.data.data?.content;
      setProximos(Array.isArray(data) ? data : []);
    } catch { setProximos([]); }
    finally { setLoadingAg(false); }
  }

  function handleLogout() { logout(); router.push("/login"); }

  if (!mounted) return <div className="min-h-screen bg-slate-50 flex items-center justify-center"><div className="text-slate-400 text-sm">Carregando...</div></div>;

  const func = isFuncionario();
  const menu = isAdmin() ? MENU_ADMIN : func ? MENU_FUNCIONARIO : MENU_PACIENTE;
  const roleLabel = role === "ROLE_ADMIN" ? "Administrador" : func ? "Funcionário" : "Paciente";
  const roleColor = role === "ROLE_ADMIN" ? "bg-purple-100 text-purple-700" : func ? "bg-blue-100 text-blue-700" : "bg-green-100 text-green-700";

  // O card de agendamentos some quando está na view de agendamentos
  const showAgendamentosCard = view !== "agendamentos";

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white border-b border-slate-100 shadow-sm sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <button onClick={() => setView("inicio")} className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center"><span className="text-white text-xs font-bold">V</span></div>
            <div className="text-left">
              <span className="font-semibold text-slate-800 text-sm">Vita Clínica</span>
              <span className={`ml-2 text-xs px-2 py-0.5 rounded-full font-medium ${roleColor}`}>{roleLabel}</span>
            </div>
          </button>
          <div className="flex items-center gap-3">
            <span className="text-sm text-slate-600 hidden sm:block">Olá, <strong>{nome?.split(" ")[0]}</strong></span>
            <button onClick={handleLogout} className="text-sm text-slate-400 hover:text-red-600 transition px-3 py-1.5 rounded-lg hover:bg-red-50">Sair</button>
          </div>
        </div>
      </header>

      {/* Layout 3 zonas */}
      <main className="max-w-7xl mx-auto px-4 py-6">
        <div className="grid grid-cols-12 gap-5">

          {/* ZONA 1: Cards de menu (esquerda) */}
          <aside className="col-span-12 lg:col-span-3 space-y-3">
            {menu.map((item) => (
              <button
                key={item.id}
                onClick={() => setView(item.id)}
                className={`w-full text-left rounded-xl p-4 transition shadow-sm ${
                  view === item.id
                    ? "bg-primary text-white"
                    : "bg-white border border-slate-100 hover:border-primary/30 hover:shadow-md"
                }`}
              >
                <div className="text-xl mb-1">{item.emoji}</div>
                <div className={`font-semibold text-sm ${view === item.id ? "text-white" : "text-slate-800"}`}>{item.title}</div>
                <div className={`text-xs mt-0.5 ${view === item.id ? "text-blue-100" : "text-slate-400"}`}>{item.desc}</div>
              </button>
            ))}
          </aside>

          {/* ZONA 2: Conteúdo (centro) */}
          <section className={`col-span-12 ${showAgendamentosCard ? "lg:col-span-6" : "lg:col-span-9"}`}>
            {view === "inicio" && (
              <div className="bg-white rounded-2xl border border-slate-100 p-8">
                <h2 className="text-xl font-semibold text-slate-800 mb-3">
                  {func ? `Bem-vindo, ${nome?.split(" ")[0]}! 🏥` : `Olá, ${nome?.split(" ")[0]}! 👋`}
                </h2>
                {func ? (
                  <div className="space-y-3 text-sm text-slate-600">
                    <p>Este é o painel de gestão da <strong>Vita Clínica</strong>. Use os cards à esquerda para navegar:</p>
                    <ul className="space-y-2 mt-2">
                      <li className="flex gap-2"><span>📅</span><span><strong>Novo Agendamento</strong> — criar consultas para pacientes, funcionários ou para você.</span></li>
                      <li className="flex gap-2"><span>📋</span><span><strong>Agendamentos</strong> — visualizar, filtrar e cancelar consultas.</span></li>
                      <li className="flex gap-2"><span>👥</span><span><strong>Pacientes</strong> — buscar e cadastrar pacientes.</span></li>
                      {isAdmin() && <li className="flex gap-2"><span>👨‍⚕️</span><span><strong>Profissionais</strong> — cadastrar médicos e especialidades.</span></li>}
                    </ul>
                    <p className="mt-3 text-slate-500">À direita você acompanha os <strong>próximos agendamentos</strong> da clínica.</p>
                  </div>
                ) : (
                  <div className="space-y-3 text-sm text-slate-600">
                    <p>Bem-vindo à <strong>Vita Clínica</strong>! Cuide da sua saúde com praticidade. Use os cards à esquerda:</p>
                    <ul className="space-y-2 mt-2">
                      <li className="flex gap-2"><span>📅</span><span><strong>Nova Consulta</strong> — agende com o profissional que precisar.</span></li>
                      <li className="flex gap-2"><span>📋</span><span><strong>Meus Agendamentos</strong> — veja seu histórico e cancele consultas.</span></li>
                      <li className="flex gap-2"><span>👤</span><span><strong>Meu Perfil</strong> — mantenha seus dados atualizados.</span></li>
                    </ul>
                    <p className="mt-3 text-slate-500">À direita você vê suas <strong>próximas consultas</strong>.</p>
                  </div>
                )}
              </div>
            )}
            {view === "novo" && userId && <PanelNovoAgendamento userId={userId} isFuncionario={func} onSucesso={() => { setView("agendamentos"); loadProximos(); }} />}
            {view === "agendamentos" && <PanelAgendamentos isFuncionario={func} />}
            {view === "perfil" && userId && <PanelPerfil userId={userId} />}
            {view === "pacientes" && <PanelPacientes />}
            {view === "profissionais" && <PanelProfissionais />}
          </section>

          {/* ZONA 3: Card de próximos agendamentos (direita, fixo) */}
          {showAgendamentosCard && (
            <aside className="col-span-12 lg:col-span-3">
              <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden sticky top-20">
                <div className="px-4 py-3 border-b border-slate-50 bg-slate-50/50">
                  <h3 className="font-semibold text-slate-700 text-xs uppercase tracking-wide">
                    {func ? "📋 Próximos da Clínica" : "📅 Minhas Consultas"}
                  </h3>
                </div>
                {loadingAg ? (
                  <div className="py-10 text-center text-slate-400 text-xs">Carregando...</div>
                ) : proximos.length === 0 ? (
                  <div className="py-10 text-center px-4">
                    <div className="text-3xl mb-2">📭</div>
                    <p className="text-slate-400 text-xs">Nenhum agendamento futuro.</p>
                  </div>
                ) : (
                  <div className="divide-y divide-slate-50 max-h-[500px] overflow-y-auto">
                    {proximos.filter(a => a.status === "AGENDADO" || a.status === "CONFIRMADO").map((a) => (
                      <div key={a.id} className="px-4 py-3">
                        <div className="text-sm font-medium text-slate-800 truncate">
                          {func ? a.pacienteNome : a.profissionalNome}
                        </div>
                        <div className="text-xs text-slate-400 mt-0.5">{a.especialidadeNome}</div>
                        <div className="text-xs text-primary font-medium mt-1">
                          {new Date(a.dataConsulta + "T00:00:00").toLocaleDateString("pt-BR", { day: "2-digit", month: "short" })} às {a.horaConsulta?.substring(0, 5)}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </aside>
          )}
        </div>
      </main>
    </div>
  );
}
