"use client";
import { useEffect, useState } from "react";
import { agendamentoApi } from "@/lib/api";
import type { Agendamento, StatusAgendamento } from "@/types";

const COLORS:Record<string,string>={AGENDADO:"bg-blue-50 text-blue-700 border-blue-200",CONFIRMADO:"bg-green-50 text-green-700 border-green-200",CANCELADO:"bg-red-50 text-red-600 border-red-200",CONCLUIDO:"bg-slate-50 text-slate-600 border-slate-200",NAO_COMPARECEU:"bg-orange-50 text-orange-700 border-orange-200"};
const LABEL:Record<string,string>={AGENDADO:"Agendado",CONFIRMADO:"Confirmado",CANCELADO:"Cancelado",CONCLUIDO:"Concluído",NAO_COMPARECEU:"Não Compareceu"};

export default function PanelAgendamentos({ isFuncionario }: { isFuncionario: boolean; }) {
  const [ags, setAgs] = useState<Agendamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtro, setFiltro] = useState<StatusAgendamento|"">("");
  const [cancId, setCancId] = useState<number|null>(null);
  const [motivo, setMotivo] = useState("");

  useEffect(()=>{ carregar(); }, [filtro]);

  async function carregar(){
    setLoading(true);
    try{
      const res = isFuncionario
        ? await agendamentoApi.listar({page:0,size:50,status:filtro||undefined})
        : await agendamentoApi.meus();
      setAgs(res.data.data?.content||[]);
    }catch{setAgs([]);}finally{setLoading(false);}
  }
  async function cancelar(id:number){if(motivo.length<10)return;try{await agendamentoApi.cancelar(id,motivo);setCancId(null);setMotivo("");carregar();}catch{alert("Erro ao cancelar");}}
  function fd(d:string){return new Date(d+"T00:00:00").toLocaleDateString("pt-BR",{weekday:"short",day:"2-digit",month:"short"});}

  return (
    <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
      <div className="px-5 py-4 border-b border-slate-50 flex items-center justify-between">
        <h2 className="font-semibold text-slate-800 text-sm">{isFuncionario?"Todos os Agendamentos":"Meus Agendamentos"}</h2>
        <select value={filtro} onChange={e=>setFiltro(e.target.value as StatusAgendamento|"")} className="text-xs border border-slate-200 rounded-lg px-2 py-1.5">
          <option value="">Todos</option><option value="AGENDADO">Agendado</option><option value="CONFIRMADO">Confirmado</option><option value="CANCELADO">Cancelado</option><option value="CONCLUIDO">Concluído</option>
        </select>
      </div>
      {loading?(<div className="py-16 text-center text-slate-400 text-sm">Carregando...</div>):ags.length===0?(
        <div className="py-16 text-center"><div className="text-4xl mb-3">📭</div><p className="text-slate-500 text-sm">Nenhum agendamento encontrado.</p></div>
      ):(
        <div className="divide-y divide-slate-50 max-h-[600px] overflow-y-auto">
          {ags.map(a=>(
            <div key={a.id} className="px-5 py-3.5">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-0.5 flex-wrap">
                    <span className="font-medium text-slate-800 text-sm">{isFuncionario?a.pacienteNome:a.profissionalNome}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full border ${COLORS[a.status]}`}>{LABEL[a.status]}</span>
                  </div>
                  <div className="text-xs text-slate-400">{a.especialidadeNome} · <span className="font-medium text-slate-600">{fd(a.dataConsulta)} às {a.horaConsulta?.substring(0,5)}</span> · {a.tipoAtendimento==="PRESENCIAL"?"Presencial":"Virtual"}</div>
                  {a.motivoCancelamento && <p className="text-xs text-red-600 mt-1">Motivo: {a.motivoCancelamento}</p>}
                </div>
                {(a.status==="AGENDADO"||a.status==="CONFIRMADO")&&(
                  <div className="shrink-0">
                    {cancId===a.id?(
                      <div className="text-right">
                        <textarea value={motivo} onChange={e=>setMotivo(e.target.value)} placeholder="Motivo (mín. 10 chars)" rows={2} className="w-44 text-xs border border-slate-200 rounded-lg px-2 py-1.5 resize-none mb-1.5"/>
                        <div className="flex gap-2 justify-end">
                          <button onClick={()=>{setCancId(null);setMotivo("");}} className="text-xs text-slate-500">Voltar</button>
                          <button onClick={()=>cancelar(a.id)} disabled={motivo.length<10} className="text-xs px-2 py-1 bg-red-600 text-white rounded-lg disabled:opacity-40">Confirmar</button>
                        </div>
                      </div>
                    ):(
                      <button onClick={()=>{setCancId(a.id);setMotivo("");}} className="text-xs text-red-600 hover:underline">Cancelar</button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
