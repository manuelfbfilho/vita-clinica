"use client";
import { useEffect, useState } from "react";
import { pacienteApi, cepApi } from "@/lib/api";

function fTel(v:string){const n=v.replace(/\D/g,"").slice(0,11);return n.length<=10?n.replace(/(\d{2})(\d{4})(\d{0,4})/,"($1) $2-$3"):n.replace(/(\d{2})(\d{5})(\d{0,4})/,"($1) $2-$3");}

export default function PanelPerfil({ userId }: { userId: number; }) {
  const [loading,setLoading]=useState(true);
  const [salvando,setSalvando]=useState(false);
  const [ok,setOk]=useState(false);
  const [erro,setErro]=useState("");
  const [f,setF]=useState({nomeCompleto:"",cpf:"",email:"",telefone:"",cep:"",logradouro:"",numero:"",complemento:"",bairro:"",cidade:"",uf:"",planoSaudeNome:""});
  const set=(k:string,v:string)=>setF(p=>({...p,[k]:v}));

  useEffect(()=>{carregar();},[]);
  async function carregar(){try{const r=await pacienteApi.buscar(userId);const d=r.data.data;setF({nomeCompleto:d.nomeCompleto||"",cpf:d.cpf||"",email:d.email||"",telefone:d.telefone||"",cep:d.cep||"",logradouro:d.logradouro||"",numero:d.numero||"",complemento:d.complemento||"",bairro:d.bairro||"",cidade:d.cidade||"",uf:d.uf||"",planoSaudeNome:d.planoSaudeNome||""});}catch{setErro("Erro ao carregar perfil");}finally{setLoading(false);}}
  async function buscarCep(cep:string){const n=cep.replace(/\D/g,"");if(n.length!==8)return;try{const r=await cepApi.buscar(n);const d=r.data.data;setF(p=>({...p,logradouro:d.logradouro||p.logradouro,bairro:d.bairro||p.bairro,cidade:d.cidade||p.cidade,uf:d.uf||p.uf}));}catch{}}
  async function salvar(e:React.FormEvent){e.preventDefault();setSalvando(true);setErro("");setOk(false);try{await pacienteApi.atualizar(userId,{nomeCompleto:f.nomeCompleto,email:f.email,telefone:f.telefone,cep:f.cep,logradouro:f.logradouro,numero:f.numero,complemento:f.complemento,bairro:f.bairro,cidade:f.cidade,uf:f.uf});setOk(true);setTimeout(()=>setOk(false),3000);}catch(err:unknown){const d=(err as {response?:{data?:{mensagem?:string}}})?.response?.data;setErro(d?.mensagem||"Erro ao salvar");}finally{setSalvando(false);}}

  if(loading)return <div className="bg-white rounded-2xl border border-slate-100 p-16 text-center text-slate-400 text-sm">Carregando perfil...</div>;

  return (
    <form onSubmit={salvar} className="space-y-4">
      {ok && <div className="p-3 rounded-lg bg-green-50 border border-green-200 text-green-700 text-sm">✅ Dados atualizados!</div>}
      {erro && <div className="p-3 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{erro}</div>}
      <div className="bg-white rounded-2xl border border-slate-100 p-6">
        <h2 className="font-semibold text-slate-800 text-sm mb-4">Meus Dados</h2>
        <div className="space-y-3">
          <div><label className="block text-xs font-medium text-slate-600 mb-1">Nome completo</label><input value={f.nomeCompleto} onChange={e=>set("nomeCompleto",e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
          <div className="grid grid-cols-2 gap-3">
            <div><label className="block text-xs font-medium text-slate-600 mb-1">CPF</label><input value={f.cpf} readOnly className="w-full px-3 py-2 text-sm border border-slate-100 rounded-lg bg-slate-50 text-slate-400"/></div>
            <div><label className="block text-xs font-medium text-slate-600 mb-1">Telefone</label><input value={f.telefone} onChange={e=>set("telefone",fTel(e.target.value))} maxLength={15} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
          </div>
          <div><label className="block text-xs font-medium text-slate-600 mb-1">Email</label><input type="email" value={f.email} onChange={e=>set("email",e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
          {f.planoSaudeNome && <div><label className="block text-xs font-medium text-slate-600 mb-1">Plano de saúde</label><input value={f.planoSaudeNome} readOnly className="w-full px-3 py-2 text-sm border border-slate-100 rounded-lg bg-slate-50 text-slate-500"/></div>}
        </div>
      </div>
      <div className="bg-white rounded-2xl border border-slate-100 p-6">
        <h2 className="font-semibold text-slate-800 text-sm mb-4">Endereço</h2>
        <div className="space-y-3">
          <div className="flex gap-3">
            <div className="flex-1"><label className="block text-xs font-medium text-slate-600 mb-1">CEP</label><input value={f.cep} onChange={e=>{set("cep",e.target.value);buscarCep(e.target.value);}} maxLength={9} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
            <div className="w-24"><label className="block text-xs font-medium text-slate-600 mb-1">Número</label><input value={f.numero} onChange={e=>set("numero",e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
          </div>
          <input value={f.logradouro} onChange={e=>set("logradouro",e.target.value)} placeholder="Logradouro" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
          <input value={f.complemento} onChange={e=>set("complemento",e.target.value)} placeholder="Complemento" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-2"><input value={f.bairro} onChange={e=>set("bairro",e.target.value)} placeholder="Bairro" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/></div>
            <input value={f.uf} onChange={e=>set("uf",e.target.value)} maxLength={2} placeholder="UF" className="px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
          </div>
          <input value={f.cidade} onChange={e=>set("cidade",e.target.value)} placeholder="Cidade" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20"/>
        </div>
      </div>
      <button type="submit" disabled={salvando} className="w-full py-2.5 bg-primary text-white rounded-xl font-medium text-sm hover:bg-primary-light transition disabled:opacity-60">{salvando?"Salvando...":"Salvar alterações"}</button>
    </form>
  );
}
