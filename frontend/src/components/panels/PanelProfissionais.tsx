"use client";
import { useEffect, useState } from "react";
import { profissionalApi, especialidadeApi } from "@/lib/api";
import type { Profissional, Especialidade } from "@/types";

export default function PanelProfissionais() {
  const [view,setView]=useState<"lista"|"cadastro">("lista");
  const [profs,setProfs]=useState<Profissional[]>([]);
  const [esps,setEsps]=useState<Especialidade[]>([]);
  const [loading,setLoading]=useState(true);
  const [ok,setOk]=useState(false);
  const [erro,setErro]=useState("");
  const [salvando,setSalvando]=useState(false);
  const [f,setF]=useState({nome:"",crm:"",especialidadeId:"",valorConsulta:""});
  const set=(k:string,v:string)=>setF(p=>({...p,[k]:v}));

  useEffect(()=>{carregar();especialidadeApi.listar().then(r=>setEsps(r.data.data||[]));},[]);
  async function carregar(){setLoading(true);try{const r=await profissionalApi.listar();setProfs(r.data.data||[]);}catch{setProfs([]);}finally{setLoading(false);}}
  async function cadastrar(e:React.FormEvent){e.preventDefault();setSalvando(true);setErro("");try{await profissionalApi.cadastrar({nome:f.nome,crm:f.crm,especialidadeId:Number(f.especialidadeId),valorConsulta:f.valorConsulta?Number(f.valorConsulta):undefined});setOk(true);setF({nome:"",crm:"",especialidadeId:"",valorConsulta:""});setTimeout(()=>{setOk(false);setView("lista");carregar();},1500);}catch(err:unknown){const d=(err as {response?:{data?:{mensagem?:string;campos?:Record<string,string>}}})?.response?.data;setErro(d?.campos?Object.values(d.campos).join(" · "):d?.mensagem||"Erro ao cadastrar");}finally{setSalvando(false);}}

  if(view==="cadastro")return(
    <div className="bg-white rounded-2xl border border-slate-100 p-6">
      <div className="flex items-center gap-3 mb-5"><button onClick={()=>setView("lista")} className="text-slate-400 hover:text-primary">←</button><h2 className="font-semibold text-slate-800 text-sm">Cadastrar Profissional</h2></div>
      {ok && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">✅ Profissional cadastrado!</div>}
      {erro && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{erro}</div>}
      <form onSubmit={cadastrar} className="space-y-3">
        <input required value={f.nome} onChange={e=>set("nome",e.target.value)} placeholder="Nome do profissional *" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <input required value={f.crm} onChange={e=>set("crm",e.target.value)} placeholder="CRM * (ex: CRM/PE-12345)" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <select required value={f.especialidadeId} onChange={e=>set("especialidadeId",e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg"><option value="">Selecione a especialidade *</option>{esps.map(e=><option key={e.id} value={e.id}>{e.nome}</option>)}</select>
        <input type="number" step="0.01" value={f.valorConsulta} onChange={e=>set("valorConsulta",e.target.value)} placeholder="Valor da consulta (R$)" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <button type="submit" disabled={salvando} className="w-full py-2.5 bg-primary text-white rounded-lg font-medium text-sm hover:bg-primary-light disabled:opacity-60">{salvando?"Cadastrando...":"Cadastrar Profissional"}</button>
      </form>
    </div>
  );

  return(
    <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
      <div className="px-5 py-4 border-b border-slate-50 flex items-center justify-between">
        <h2 className="font-semibold text-slate-800 text-sm">Profissionais</h2>
        <button onClick={()=>setView("cadastro")} className="px-3 py-1.5 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light">+ Novo</button>
      </div>
      {loading?(<div className="py-16 text-center text-slate-400 text-sm">Carregando...</div>):(
        <div className="divide-y divide-slate-50 max-h-[600px] overflow-y-auto">
          {profs.map(p=>(
            <div key={p.id} className="px-5 py-3.5 flex items-center justify-between">
              <div><div className="font-medium text-slate-800 text-sm">{p.nome}</div><div className="text-xs text-slate-400">{p.crm} · {p.especialidadeNome}</div></div>
              {p.valorConsulta && <span className="text-xs text-secondary font-medium">R$ {p.valorConsulta.toFixed(2).replace(".",",")}</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
