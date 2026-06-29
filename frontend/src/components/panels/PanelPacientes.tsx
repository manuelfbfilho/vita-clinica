"use client";
import { useEffect, useState } from "react";
import { pacienteApi, cepApi, planoApi } from "@/lib/api";

function fCpf(v:string){const n=v.replace(/\D/g,"").slice(0,11);return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/,"$1.$2.$3-$4").replace(/(\d{3})(\d{3})(\d{3})/,"$1.$2.$3").replace(/(\d{3})(\d{3})/,"$1.$2");}
function fTel(v:string){const n=v.replace(/\D/g,"").slice(0,11);return n.length<=10?n.replace(/(\d{2})(\d{4})(\d{0,4})/,"($1) $2-$3"):n.replace(/(\d{2})(\d{5})(\d{0,4})/,"($1) $2-$3");}

export default function PanelPacientes() {
  const [view,setView]=useState<"lista"|"cadastro">("lista");
  const [pacientes,setPacientes]=useState<{id:number;nomeCompleto:string;cpf:string;email:string;ativo:boolean}[]>([]);
  const [busca,setBusca]=useState("");
  const [loading,setLoading]=useState(true);
  const [planos,setPlanos]=useState<{id:number;nome:string}[]>([]);
  const [ok,setOk]=useState(false);
  const [erro,setErro]=useState("");
  const [salvando,setSalvando]=useState(false);
  const [f,setF]=useState({nomeCompleto:"",cpf:"",email:"",telefone:"",senha:"",cep:"",logradouro:"",numero:"",bairro:"",cidade:"",uf:"",planoSaudeId:""});
  const set=(k:string,v:string)=>setF(p=>({...p,[k]:v}));

  useEffect(()=>{carregar();planoApi.listar().then(r=>setPlanos(r.data.data||[]));},[]);
  async function carregar(termo=""){setLoading(true);try{const r=await pacienteApi.listar(termo);setPacientes(r.data.data?.content||[]);}catch{setPacientes([]);}finally{setLoading(false);}}
  function onBusca(v:string){setBusca(v);if(v.length===0||v.length>=3)carregar(v);}
  async function buscarCep(cep:string){const n=cep.replace(/\D/g,"");if(n.length!==8)return;try{const r=await cepApi.buscar(n);const d=r.data.data;setF(p=>({...p,logradouro:d.logradouro||"",bairro:d.bairro||"",cidade:d.cidade||"",uf:d.uf||""}));}catch{}}
  async function cadastrar(e:React.FormEvent){e.preventDefault();setSalvando(true);setErro("");try{await pacienteApi.cadastrar({...f,planoSaudeId:f.planoSaudeId?Number(f.planoSaudeId):undefined});setOk(true);setF({nomeCompleto:"",cpf:"",email:"",telefone:"",senha:"",cep:"",logradouro:"",numero:"",bairro:"",cidade:"",uf:"",planoSaudeId:""});setTimeout(()=>{setOk(false);setView("lista");carregar();},1500);}catch(err:unknown){const d=(err as {response?:{data?:{mensagem?:string;campos?:Record<string,string>}}})?.response?.data;setErro(d?.campos?Object.values(d.campos).join(" · "):d?.mensagem||"Erro ao cadastrar");}finally{setSalvando(false);}}

  if(view==="cadastro")return(
    <div className="bg-white rounded-2xl border border-slate-100 p-6">
      <div className="flex items-center gap-3 mb-5"><button onClick={()=>setView("lista")} className="text-slate-400 hover:text-primary">←</button><h2 className="font-semibold text-slate-800 text-sm">Cadastrar Paciente</h2></div>
      {ok && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">✅ Paciente cadastrado!</div>}
      {erro && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{erro}</div>}
      <form onSubmit={cadastrar} className="space-y-3">
        <input required value={f.nomeCompleto} onChange={e=>set("nomeCompleto",e.target.value)} placeholder="Nome completo *" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <div className="grid grid-cols-2 gap-3">
          <input required value={f.cpf} onChange={e=>set("cpf",fCpf(e.target.value))} maxLength={14} placeholder="CPF *" className="px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
          <input required value={f.telefone} onChange={e=>set("telefone",fTel(e.target.value))} maxLength={15} placeholder="Telefone *" className="px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        </div>
        <input required type="email" value={f.email} onChange={e=>set("email",e.target.value)} placeholder="Email *" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <input required type="password" value={f.senha} onChange={e=>set("senha",e.target.value)} placeholder="Senha * (mín. 10 chars, maiúscula, número, especial)" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <select value={f.planoSaudeId} onChange={e=>set("planoSaudeId",e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg"><option value="">Sem plano de saúde</option>{planos.map(p=><option key={p.id} value={p.id}>{p.nome}</option>)}</select>
        <div className="flex gap-3">
          <input value={f.cep} onChange={e=>{set("cep",e.target.value);buscarCep(e.target.value);}} maxLength={9} placeholder="CEP" className="flex-1 px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
          <input value={f.numero} onChange={e=>set("numero",e.target.value)} placeholder="Nº" className="w-20 px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        </div>
        <input value={f.logradouro} onChange={e=>set("logradouro",e.target.value)} placeholder="Logradouro" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <div className="grid grid-cols-3 gap-3"><div className="col-span-2"><input value={f.bairro} onChange={e=>set("bairro",e.target.value)} placeholder="Bairro" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div><input value={f.uf} onChange={e=>set("uf",e.target.value)} maxLength={2} placeholder="UF" className="px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
        <input value={f.cidade} onChange={e=>set("cidade",e.target.value)} placeholder="Cidade" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <button type="submit" disabled={salvando} className="w-full py-2.5 bg-primary text-white rounded-lg font-medium text-sm hover:bg-primary-light disabled:opacity-60">{salvando?"Cadastrando...":"Cadastrar Paciente"}</button>
      </form>
    </div>
  );

  return(
    <div className="bg-white rounded-2xl border border-slate-100 overflow-hidden">
      <div className="px-5 py-4 border-b border-slate-50 flex items-center gap-3">
        <input value={busca} onChange={e=>onBusca(e.target.value)} placeholder="Buscar por nome ou CPF..." className="flex-1 px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        <button onClick={()=>setView("cadastro")} className="px-3 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light whitespace-nowrap">+ Novo</button>
      </div>
      {loading?(<div className="py-16 text-center text-slate-400 text-sm">Carregando...</div>):pacientes.length===0?(
        <div className="py-16 text-center"><div className="text-4xl mb-3">👥</div><p className="text-slate-500 text-sm">{busca?`Nenhum paciente para "${busca}".`:"Nenhum paciente cadastrado."}</p><button onClick={()=>setView("cadastro")} className="mt-3 text-sm text-primary hover:underline">Cadastrar novo paciente</button></div>
      ):(
        <div className="divide-y divide-slate-50 max-h-[600px] overflow-y-auto">
          {pacientes.map(p=>(
            <div key={p.id} className="px-5 py-3.5 flex items-center justify-between">
              <div><div className="font-medium text-slate-800 text-sm">{p.nomeCompleto}</div><div className="text-xs text-slate-400">{p.cpf} · {p.email}</div></div>
              {!p.ativo && <span className="text-xs text-red-500 bg-red-50 px-2 py-0.5 rounded-full">Inativo</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
