"use client";
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { especialidadeApi, profissionalApi, agendamentoApi } from "@/lib/api";
import type { Especialidade, Profissional, SlotHorario } from "@/types";

type Step = 1 | 2 | 3 | 4 | 5;

const DIAS_SEMANA = ["Dom","Seg","Ter","Qua","Qui","Sex","Sáb"];

function prox30Dias(): string[] {
  const dias = [];
  for (let i = 1; i <= 30; i++) {
    const d = new Date(); d.setDate(d.getDate() + i);
    if (d.getDay() !== 0) dias.push(d.toISOString().split("T")[0]);
  }
  return dias;
}

export default function NovoAgendamentoPage() {
  const router = useRouter();
  const { userId, isFuncionario } = useAuthStore();

  const [step, setStep]                   = useState<Step>(1);
  const [especialidades, setEspecialidades] = useState<Especialidade[]>([]);
  const [profissionais, setProfissionais]   = useState<Profissional[]>([]);
  const [slots, setSlots]                   = useState<SlotHorario[]>([]);
  const [loading, setLoading]               = useState(false);
  const [erro, setErro]                     = useState("");

  const [espSelected, setEspSelected]       = useState<Especialidade | null>(null);
  const [profSelected, setProfSelected]     = useState<Profissional | null>(null);
  const [dataSelected, setDataSelected]     = useState("");
  const [horaSelected, setHoraSelected]     = useState("");
  const [tipoAtendimento, setTipoAtendimento] = useState("PRESENCIAL");
  const [tipoConsulta, setTipoConsulta]     = useState("CONSULTA");
  const [formaPagamento, setFormaPagamento] = useState("PARTICULAR");
  const [observacao, setObservacao]         = useState("");

  const dias = prox30Dias();

  useEffect(() => {
    if (!userId) { router.push("/login"); return; }
    especialidadeApi.listar().then(r => setEspecialidades(r.data.data || []));
  }, [userId]);

  async function selecionarEsp(esp: Especialidade) {
    setEspSelected(esp); setProfSelected(null); setLoading(true);
    try {
      const r = await profissionalApi.listar(esp.id);
      setProfissionais(r.data.data || r.data || []);
      setStep(2);
    } catch { setErro("Erro ao carregar profissionais."); }
    finally { setLoading(false); }
  }

  async function selecionarData(data: string) {
    if (!profSelected) return;
    setDataSelected(data); setHoraSelected(""); setLoading(true);
    try {
      const r = await agendamentoApi.slots(profSelected.id, data);
      setSlots(r.data.data || []);
      setStep(4);
    } catch { setErro("Erro ao carregar horários."); }
    finally { setLoading(false); }
  }

  async function confirmarAgendamento() {
    if (!profSelected || !dataSelected || !horaSelected || !userId) return;
    setLoading(true); setErro("");
    try {
      await agendamentoApi.criar({
        pacienteId: userId,
        profissionalId: profSelected.id,
        dataConsulta: dataSelected,
        horaConsulta: horaSelected + ":00",
        tipoAtendimento,
        tipoConsulta,
        formaPagamento,
        observacao: observacao || undefined,
      });
      setStep(5);
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem;
      setErro(msg || "Erro ao criar agendamento. Tente novamente.");
    } finally { setLoading(false); }
  }

  const selecionarProf = (p: Profissional) => { setProfSelected(p); setStep(3); };

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push("/dashboard")} className="text-slate-400 hover:text-slate-700">←</button>
          <div>
            <h1 className="font-semibold text-slate-800 text-sm">Novo Agendamento</h1>
            <p className="text-xs text-slate-400">Etapa {step < 5 ? step : 5} de 5</p>
          </div>
        </div>
        {/* Progress bar */}
        <div className="max-w-2xl mx-auto px-4 pb-3">
          <div className="h-1 bg-slate-100 rounded-full overflow-hidden">
            <div className="h-full bg-primary transition-all" style={{ width: `${(step / 5) * 100}%` }} />
          </div>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-8">
        {erro && <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{erro}</div>}

        {/* STEP 1: Especialidade */}
        {step === 1 && (
          <div>
            <h2 className="font-semibold text-slate-800 mb-1">Selecione a especialidade</h2>
            <p className="text-sm text-slate-500 mb-5">Escolha a área médica para sua consulta</p>
            {loading ? <p className="text-slate-400">Carregando...</p> : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {especialidades.map(e => (
                  <button key={e.id} onClick={() => selecionarEsp(e)}
                    className="bg-white border border-slate-100 rounded-xl p-4 text-left hover:border-primary/40 hover:shadow-sm transition">
                    <div className="font-medium text-slate-800 text-sm">{e.nome}</div>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* STEP 2: Profissional */}
        {step === 2 && (
          <div>
            <div className="flex items-center gap-2 mb-5">
              <button onClick={() => setStep(1)} className="text-slate-400 hover:text-primary text-sm">← Especialidade</button>
              <span className="text-slate-300">/</span>
              <span className="text-sm font-medium text-slate-700">{espSelected?.nome}</span>
            </div>
            <h2 className="font-semibold text-slate-800 mb-1">Selecione o profissional</h2>
            <p className="text-sm text-slate-500 mb-5">Escolha o médico para sua consulta</p>
            <div className="space-y-3">
              {profissionais.map(p => (
                <button key={p.id} onClick={() => selecionarProf(p)}
                  className="w-full bg-white border border-slate-100 rounded-xl p-4 text-left hover:border-primary/40 hover:shadow-sm transition">
                  <div className="font-medium text-slate-800 text-sm">{p.nome}</div>
                  <div className="text-xs text-slate-400 mt-0.5">{p.crm}</div>
                  {p.valorConsulta && (
                    <div className="text-xs text-secondary font-medium mt-1">
                      R$ {p.valorConsulta.toFixed(2).replace(".", ",")} — consulta particular
                    </div>
                  )}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 3: Data */}
        {step === 3 && (
          <div>
            <div className="flex items-center gap-2 mb-5">
              <button onClick={() => setStep(2)} className="text-slate-400 hover:text-primary text-sm">← Profissional</button>
              <span className="text-slate-300">/</span>
              <span className="text-sm font-medium text-slate-700">{profSelected?.nome}</span>
            </div>
            <h2 className="font-semibold text-slate-800 mb-1">Selecione a data</h2>
            <p className="text-sm text-slate-500 mb-5">Próximos 30 dias disponíveis</p>
            <div className="grid grid-cols-4 sm:grid-cols-7 gap-2">
              {dias.map(d => {
                const dt = new Date(d + "T00:00:00");
                const diaSemana = DIAS_SEMANA[dt.getDay()];
                const diaNum = dt.getDate();
                const mes = dt.toLocaleString("pt-BR", { month: "short" });
                return (
                  <button key={d} onClick={() => selecionarData(d)}
                    className="bg-white border border-slate-100 rounded-xl p-2.5 text-center hover:border-primary hover:bg-blue-50 transition">
                    <div className="text-xs text-slate-400">{diaSemana}</div>
                    <div className="text-base font-semibold text-slate-800">{diaNum}</div>
                    <div className="text-xs text-slate-400">{mes}</div>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* STEP 4: Horário */}
        {step === 4 && (
          <div>
            <div className="flex items-center gap-2 mb-5">
              <button onClick={() => setStep(3)} className="text-slate-400 hover:text-primary text-sm">← Data</button>
              <span className="text-slate-300">/</span>
              <span className="text-sm font-medium text-slate-700">
                {new Date(dataSelected + "T00:00:00").toLocaleDateString("pt-BR")}
              </span>
            </div>
            <h2 className="font-semibold text-slate-800 mb-1">Selecione o horário</h2>
            <p className="text-sm text-slate-500 mb-5">Horários disponíveis para {profSelected?.nome}</p>
            {loading ? <p className="text-slate-400">Carregando horários...</p> : (
              <div className="grid grid-cols-4 sm:grid-cols-6 gap-2 mb-8">
                {slots.map(s => (
                  <button key={s.horaFormatada}
                    onClick={() => { if (s.disponivel) setHoraSelected(s.horaFormatada); }}
                    disabled={!s.disponivel}
                    className={`py-2 px-3 rounded-lg text-sm font-medium border transition ${
                      horaSelected === s.horaFormatada
                        ? "bg-primary text-white border-primary"
                        : s.disponivel
                          ? "bg-white border-slate-200 text-slate-700 hover:border-primary/40"
                          : "bg-slate-50 border-slate-100 text-slate-300 cursor-not-allowed line-through"
                    }`}>
                    {s.horaFormatada}
                  </button>
                ))}
              </div>
            )}

            {horaSelected && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1.5">Tipo de atendimento</label>
                    <select value={tipoAtendimento} onChange={e => setTipoAtendimento(e.target.value)}
                      className="w-full text-sm border border-slate-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary/20">
                      <option value="PRESENCIAL">Presencial</option>
                      <option value="VIRTUAL">Virtual</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1.5">Tipo de consulta</label>
                    <select value={tipoConsulta} onChange={e => setTipoConsulta(e.target.value)}
                      className="w-full text-sm border border-slate-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary/20">
                      <option value="CONSULTA">Consulta</option>
                      <option value="RETORNO">Retorno</option>
                      <option value="ENCAIXE">Encaixe</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1.5">Forma de pagamento</label>
                  <select value={formaPagamento} onChange={e => setFormaPagamento(e.target.value)}
                    className="w-full text-sm border border-slate-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary/20">
                    <option value="PARTICULAR">Particular</option>
                    <option value="PLANO">Plano de Saúde</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1.5">Observações (opcional)</label>
                  <textarea value={observacao} onChange={e => setObservacao(e.target.value)} rows={2}
                    placeholder="Sintomas, histórico relevante..."
                    className="w-full text-sm border border-slate-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary/20 resize-none" />
                </div>
                <div className="bg-accent rounded-xl p-4 text-sm">
                  <p className="font-medium text-slate-700 mb-2">Resumo do agendamento:</p>
                  <div className="space-y-1 text-slate-600 text-xs">
                    <p>👨‍⚕️ {profSelected?.nome} — {espSelected?.nome}</p>
                    <p>📅 {new Date(dataSelected + "T00:00:00").toLocaleDateString("pt-BR")} às {horaSelected}</p>
                    <p>📍 {tipoAtendimento === "PRESENCIAL" ? "Presencial" : "Virtual"} · {tipoConsulta} · {formaPagamento === "PARTICULAR" ? "Particular" : "Plano"}</p>
                  </div>
                </div>
                <button onClick={confirmarAgendamento} disabled={loading}
                  className="w-full py-3 bg-primary text-white rounded-xl font-medium text-sm hover:bg-primary-light transition disabled:opacity-60">
                  {loading ? "Confirmando..." : "Confirmar Agendamento"}
                </button>
              </div>
            )}
          </div>
        )}

        {/* STEP 5: Sucesso */}
        {step === 5 && (
          <div className="text-center py-8">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl">✅</span>
            </div>
            <h2 className="text-xl font-semibold text-slate-800 mb-2">Consulta agendada!</h2>
            <p className="text-slate-500 text-sm mb-2">Um email de confirmação foi enviado para você.</p>
            <div className="bg-white border border-slate-100 rounded-xl p-4 text-sm text-left mb-8 max-w-xs mx-auto">
              <p className="font-medium text-slate-700">{profSelected?.nome}</p>
              <p className="text-slate-500 text-xs">{espSelected?.nome}</p>
              <p className="text-primary text-xs mt-2 font-medium">
                {new Date(dataSelected + "T00:00:00").toLocaleDateString("pt-BR")} às {horaSelected}
              </p>
            </div>
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <button onClick={() => router.push("/dashboard")}
                className="px-6 py-2.5 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light transition">
                Ver meus agendamentos
              </button>
              <button onClick={() => { setStep(1); setEspSelected(null); setProfSelected(null); setDataSelected(""); setHoraSelected(""); }}
                className="px-6 py-2.5 border border-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-50 transition">
                Novo agendamento
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
