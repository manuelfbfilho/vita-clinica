"use client";
import { useState, useEffect } from "react";
import { especialidadeApi, profissionalApi, agendamentoApi, pacienteApi } from "@/lib/api";
import type { Especialidade, Profissional, SlotHorario } from "@/types";

const DIAS = ["Dom","Seg","Ter","Qua","Qui","Sex","Sáb"];
function prox30() {
  const d=[]; for(let i=1;i<=30;i++){const x=new Date();x.setDate(x.getDate()+i);if(x.getDay()!==0)d.push(x.toISOString().split("T")[0]);}return d;
}

export default function PanelNovoAgendamento({ userId, isFuncionario, onSucesso }: { userId: number; isFuncionario: boolean; onSucesso: () => void; }) {
  const [step, setStep] = useState(1);
  const [esps, setEsps] = useState<Especialidade[]>([]);
  const [profs, setProfs] = useState<Profissional[]>([]);
  const [slots, setSlots] = useState<SlotHorario[]>([]);
  const [pacientes, setPacientes] = useState<{id:number;nomeCompleto:string;cpf:string}[]>([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  const [pacienteId, setPacienteId] = useState<number|null>(isFuncionario ? null : userId);
  const [buscaPac, setBuscaPac] = useState("");
  const [esp, setEsp] = useState<Especialidade|null>(null);
  const [prof, setProf] = useState<Profissional|null>(null);
  const [data, setData] = useState("");
  const [hora, setHora] = useState("");
  const [tipoAt, setTipoAt] = useState("PRESENCIAL");
  const [tipoCon, setTipoCon] = useState("CONSULTA");
  const [formaPag, setFormaPag] = useState("PARTICULAR");
  const [obs, setObs] = useState("");

  const dias = prox30();
  useEffect(() => { especialidadeApi.listar().then(r=>setEsps(r.data.data||[])); }, []);
  useEffect(() => {
    if(isFuncionario && buscaPac.length>=3) pacienteApi.listar(buscaPac).then(r=>setPacientes(r.data.data?.content||[]));
  }, [buscaPac, isFuncionario]);

  async function selEsp(e:Especialidade){setEsp(e);setProf(null);setLoading(true);try{const r=await profissionalApi.listar(e.id);setProfs(r.data.data||[]);setStep(isFuncionario?3:3);}catch{setErro("Erro ao carregar profissionais");}finally{setLoading(false);}}
  async function selData(d:string){if(!prof)return;setData(d);setHora("");setLoading(true);try{const r=await agendamentoApi.slots(prof.id,d);setSlots(r.data.data||[]);setStep(5);}catch{setErro("Erro ao carregar horários");}finally{setLoading(false);}}
  async function confirmar(){if(!prof||!data||!hora||!pacienteId)return;setLoading(true);setErro("");try{await agendamentoApi.criar({pacienteId,profissionalId:prof.id,dataConsulta:data,horaConsulta:hora+":00",tipoAtendimento:tipoAt,tipoConsulta:tipoCon,formaPagamento:formaPag,observacao:obs||undefined});setStep(6);}catch(e:unknown){const m=(e as {response?:{data?:{mensagem?:string}}})?.response?.data?.mensagem;setErro(m||"Erro ao criar agendamento");}finally{setLoading(false);}}

  // Passo 0 (funcionário): escolher paciente
  const totalSteps = isFuncionario ? 6 : 5;
  const stepAdjust = isFuncionario ? step : step - (step>=2?0:0);

  return (
    <div className="bg-white rounded-2xl border border-slate-100 p-6">
      <div className="flex items-center justify-between mb-1">
        <h2 className="font-semibold text-slate-800">Novo Agendamento</h2>
        <span className="text-xs text-slate-400">Etapa {step>6?6:step} de {totalSteps}</span>
      </div>
      <div className="h-1 bg-slate-100 rounded-full overflow-hidden mb-5">
        <div className="h-full bg-primary transition-all" style={{width:`${(Math.min(step,totalSteps)/totalSteps)*100}%`}}/>
      </div>
      {erro && <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{erro}</div>}

      {/* FUNCIONÁRIO: Selecionar paciente (step 1) */}
      {isFuncionario && step===1 && (
        <div>
          <h3 className="font-medium text-slate-700 text-sm mb-1">Para qual paciente?</h3>
          <p className="text-xs text-slate-500 mb-4">Busque por nome ou CPF</p>
          <input value={buscaPac} onChange={e=>setBuscaPac(e.target.value)} placeholder="Digite ao menos 3 caracteres..."
            className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 mb-3"/>
          <div className="space-y-2 max-h-64 overflow-y-auto">
            {pacientes.map(p=>(
              <button key={p.id} onClick={()=>{setPacienteId(p.id);setStep(2);}}
                className="w-full text-left bg-white border border-slate-100 rounded-lg p-3 hover:border-primary/40 transition">
                <div className="font-medium text-slate-800 text-sm">{p.nomeCompleto}</div>
                <div className="text-xs text-slate-400">{p.cpf}</div>
              </button>
            ))}
            {buscaPac.length>=3 && pacientes.length===0 && <p className="text-xs text-slate-400 text-center py-4">Nenhum paciente encontrado.</p>}
          </div>
        </div>
      )}

      {/* Step 2 (func) / 1 (pac): Especialidade */}
      {((isFuncionario && step===2) || (!isFuncionario && step===1)) && (
        <div>
          <h3 className="font-medium text-slate-700 text-sm mb-1">Especialidade</h3>
          <p className="text-xs text-slate-500 mb-4">Escolha a área médica</p>
          {loading? <p className="text-slate-400 text-sm">Carregando...</p> : (
            <div className="grid grid-cols-2 gap-2">
              {esps.map(e=>(
                <button key={e.id} onClick={()=>selEsp(e)}
                  className="bg-white border border-slate-100 rounded-lg p-3 text-left hover:border-primary/40 transition text-sm font-medium text-slate-700">
                  {e.nome}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Step 3: Profissional */}
      {step===3 && (
        <div>
          <button onClick={()=>setStep(isFuncionario?2:1)} className="text-xs text-slate-400 hover:text-primary mb-3">← {esp?.nome}</button>
          <h3 className="font-medium text-slate-700 text-sm mb-1">Profissional</h3>
          <p className="text-xs text-slate-500 mb-4">Escolha o médico</p>
          <div className="space-y-2">
            {profs.map(p=>(
              <button key={p.id} onClick={()=>{setProf(p);setStep(4);}}
                className="w-full bg-white border border-slate-100 rounded-lg p-3 text-left hover:border-primary/40 transition">
                <div className="font-medium text-slate-800 text-sm">{p.nome}</div>
                <div className="text-xs text-slate-400">{p.crm}</div>
                {p.valorConsulta && <div className="text-xs text-secondary font-medium mt-0.5">R$ {p.valorConsulta.toFixed(2).replace(".",",")}</div>}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Step 4: Data */}
      {step===4 && (
        <div>
          <button onClick={()=>setStep(3)} className="text-xs text-slate-400 hover:text-primary mb-3">← {prof?.nome}</button>
          <h3 className="font-medium text-slate-700 text-sm mb-1">Data</h3>
          <p className="text-xs text-slate-500 mb-4">Próximos 30 dias</p>
          <div className="grid grid-cols-4 gap-2">
            {dias.map(d=>{const dt=new Date(d+"T00:00:00");return(
              <button key={d} onClick={()=>selData(d)}
                className="bg-white border border-slate-100 rounded-lg p-2 text-center hover:border-primary hover:bg-blue-50 transition">
                <div className="text-xs text-slate-400">{DIAS[dt.getDay()]}</div>
                <div className="text-sm font-semibold text-slate-800">{dt.getDate()}</div>
                <div className="text-xs text-slate-400">{dt.toLocaleString("pt-BR",{month:"short"})}</div>
              </button>
            );})}
          </div>
        </div>
      )}

      {/* Step 5: Horário + detalhes */}
      {step===5 && (
        <div>
          <button onClick={()=>setStep(4)} className="text-xs text-slate-400 hover:text-primary mb-3">← {new Date(data+"T00:00:00").toLocaleDateString("pt-BR")}</button>
          <h3 className="font-medium text-slate-700 text-sm mb-1">Horário</h3>
          <p className="text-xs text-slate-500 mb-4">Disponíveis para {prof?.nome}</p>
          {loading? <p className="text-slate-400 text-sm">Carregando...</p> : (
            <div className="grid grid-cols-4 gap-2 mb-5">
              {slots.map(s=>(
                <button key={s.horaFormatada} onClick={()=>{if(s.disponivel)setHora(s.horaFormatada);}} disabled={!s.disponivel}
                  className={`py-2 rounded-lg text-sm font-medium border transition ${hora===s.horaFormatada?"bg-primary text-white border-primary":s.disponivel?"bg-white border-slate-200 text-slate-700 hover:border-primary/40":"bg-slate-50 border-slate-100 text-slate-300 line-through cursor-not-allowed"}`}>
                  {s.horaFormatada}
                </button>
              ))}
            </div>
          )}
          {hora && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <select value={tipoAt} onChange={e=>setTipoAt(e.target.value)} className="text-sm border border-slate-200 rounded-lg px-3 py-2"><option value="PRESENCIAL">Presencial</option><option value="VIRTUAL">Virtual</option></select>
                <select value={tipoCon} onChange={e=>setTipoCon(e.target.value)} className="text-sm border border-slate-200 rounded-lg px-3 py-2"><option value="CONSULTA">Consulta</option><option value="RETORNO">Retorno</option><option value="ENCAIXE">Encaixe</option></select>
              </div>
              <select value={formaPag} onChange={e=>setFormaPag(e.target.value)} className="w-full text-sm border border-slate-200 rounded-lg px-3 py-2"><option value="PARTICULAR">Particular</option><option value="PLANO">Plano de Saúde</option></select>
              <textarea value={obs} onChange={e=>setObs(e.target.value)} rows={2} placeholder="Observações (opcional)" className="w-full text-sm border border-slate-200 rounded-lg px-3 py-2 resize-none"/>
              <button onClick={confirmar} disabled={loading} className="w-full py-2.5 bg-primary text-white rounded-lg font-medium text-sm hover:bg-primary-light transition disabled:opacity-60">
                {loading?"Confirmando...":"Confirmar Agendamento"}
              </button>
            </div>
          )}
        </div>
      )}

      {/* Step 6: Sucesso */}
      {step===6 && (
        <div className="text-center py-6">
          <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3"><span className="text-2xl">✅</span></div>
          <h3 className="text-lg font-semibold text-slate-800 mb-1">Consulta agendada!</h3>
          <p className="text-slate-500 text-sm mb-5">Email de confirmação enviado.</p>
          <div className="flex gap-2 justify-center">
            <button onClick={onSucesso} className="px-5 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light">Ver agendamentos</button>
            <button onClick={()=>{setStep(isFuncionario?1:1);setEsp(null);setProf(null);setData("");setHora("");setPacienteId(isFuncionario?null:userId);}} className="px-5 py-2 border border-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-50">Novo</button>
          </div>
        </div>
      )}
    </div>
  );
}
