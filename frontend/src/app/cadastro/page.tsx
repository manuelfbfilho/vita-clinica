"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { pacienteApi, cepApi } from "@/lib/api";

function formatCpf(v: string) {
  const n = v.replace(/\D/g, "").slice(0, 11);
  return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4")
          .replace(/(\d{3})(\d{3})(\d{3})/, "$1.$2.$3").replace(/(\d{3})(\d{3})/, "$1.$2");
}
function formatTel(v: string) {
  const n = v.replace(/\D/g, "").slice(0, 11);
  if (n.length <= 10) return n.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
  return n.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
}

export default function CadastroPage() {
  const router = useRouter();
  const [form, setForm] = useState({ nomeCompleto:"", cpf:"", email:"", telefone:"", senha:"", cep:"", logradouro:"", numero:"", bairro:"", cidade:"", uf:"" });
  const [erro, setErro] = useState("");
  const [ok, setOk] = useState(false);
  const [loading, setLoading] = useState(false);

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  async function buscarCep(cep: string) {
    const n = cep.replace(/\D/g, "");
    if (n.length !== 8) return;
    try {
      const r = await cepApi.buscar(n);
      const d = r.data.data;
      setForm(f => ({ ...f, logradouro: d.logradouro || "", bairro: d.bairro || "", cidade: d.cidade || "", uf: d.uf || "" }));
    } catch {}
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault(); setErro(""); setLoading(true);
    try {
      await pacienteApi.cadastrar({ ...form, cpf: form.cpf.replace(/\D/g,"").replace(/(\d{3})(\d{3})(\d{3})(\d{2})/,"$1.$2.$3-$4") });
      setOk(true);
    } catch (err: unknown) {
      const d = (err as { response?: { data?: { mensagem?: string; campos?: Record<string, string> } } })?.response?.data;
      if (d?.campos) setErro(Object.values(d.campos).join(" · "));
      else setErro(d?.mensagem || "Erro ao criar conta.");
    } finally { setLoading(false); }
  }

  if (ok) return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
      <div className="text-center max-w-sm">
        <div className="text-5xl mb-4">✅</div>
        <h2 className="text-xl font-semibold text-slate-800 mb-2">Conta criada!</h2>
        <p className="text-slate-500 text-sm mb-6">Seu cadastro foi realizado com sucesso.</p>
        <button onClick={() => router.push("/login")} className="px-6 py-2.5 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-light transition">
          Fazer login
        </button>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-slate-50 py-8 px-4">
      <div className="max-w-lg mx-auto">
        <div className="text-center mb-6">
          <h1 className="text-2xl font-semibold text-slate-800">Vita Clínica</h1>
          <p className="text-slate-500 text-sm mt-1">Criar sua conta de paciente</p>
        </div>
        <div className="bg-white rounded-2xl border border-slate-100 p-8">
          {erro && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{erro}</div>}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div><label className="block text-xs font-medium text-slate-600 mb-1">Nome completo *</label>
              <input required value={form.nomeCompleto} onChange={e => set("nomeCompleto", e.target.value)} className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
            <div className="grid grid-cols-2 gap-3">
              <div><label className="block text-xs font-medium text-slate-600 mb-1">CPF *</label>
                <input required value={form.cpf} onChange={e => set("cpf", formatCpf(e.target.value))} maxLength={14} className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
              <div><label className="block text-xs font-medium text-slate-600 mb-1">Telefone *</label>
                <input required value={form.telefone} onChange={e => set("telefone", formatTel(e.target.value))} maxLength={15} className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
            </div>
            <div><label className="block text-xs font-medium text-slate-600 mb-1">Email *</label>
              <input required type="email" value={form.email} onChange={e => set("email", e.target.value)} className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
            <div><label className="block text-xs font-medium text-slate-600 mb-1">Senha *</label>
              <input required type="password" value={form.senha} onChange={e => set("senha", e.target.value)} placeholder="Mín. 10 chars, 1 maiúscula, 1 número, 1 especial" className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
            <div className="border-t border-slate-100 pt-4">
              <p className="text-xs font-medium text-slate-500 mb-3">Endereço (opcional)</p>
              <div className="flex gap-2 mb-3">
                <div className="flex-1"><label className="block text-xs font-medium text-slate-600 mb-1">CEP</label>
                  <input value={form.cep} onChange={e => { set("cep", e.target.value); buscarCep(e.target.value); }} maxLength={9} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
                <div className="w-20"><label className="block text-xs font-medium text-slate-600 mb-1">Número</label>
                  <input value={form.numero} onChange={e => set("numero", e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
              </div>
              <input value={form.logradouro} onChange={e => set("logradouro", e.target.value)} placeholder="Logradouro" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 mb-2" />
              <div className="grid grid-cols-3 gap-2">
                <div className="col-span-2"><input value={form.bairro} onChange={e => set("bairro", e.target.value)} placeholder="Bairro" className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" /></div>
                <input value={form.uf} onChange={e => set("uf", e.target.value)} maxLength={2} placeholder="UF" className="px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
              </div>
            </div>
            <button type="submit" disabled={loading} className="w-full py-2.5 bg-primary text-white rounded-lg font-medium text-sm hover:bg-primary-light transition disabled:opacity-60 mt-2">
              {loading ? "Criando conta..." : "Criar conta"}
            </button>
          </form>
          <div className="mt-4 text-center">
            <p className="text-sm text-slate-500">Já tem conta? <a href="/login" className="text-primary font-medium hover:underline">Entrar</a></p>
          </div>
        </div>
      </div>
    </div>
  );
}
