"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { pacienteApi, cepApi } from "@/lib/api";

function formatTel(v: string) {
  const n = v.replace(/\D/g, "").slice(0, 11);
  if (n.length <= 10) return n.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
  return n.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
}

export default function PerfilPage() {
  const router = useRouter();
  const { userId, nome: nomeStore, logout } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [sucesso, setSucesso] = useState(false);
  const [erro, setErro] = useState("");

  const [form, setForm] = useState({
    nomeCompleto: "", cpf: "", email: "", telefone: "",
    cep: "", logradouro: "", numero: "", complemento: "", bairro: "", cidade: "", uf: "",
    planoSaudeNome: "",
  });

  useEffect(() => { setMounted(true); }, []);

  useEffect(() => {
    if (!mounted) return;
    if (!userId) { router.push("/login"); return; }
    carregar();
  }, [mounted, userId]);

  async function carregar() {
    try {
      const res = await pacienteApi.buscar(userId!);
      const d = res.data.data;
      setForm({
        nomeCompleto: d.nomeCompleto || "",
        cpf: d.cpf || "",
        email: d.email || "",
        telefone: d.telefone || "",
        cep: d.cep || "",
        logradouro: d.logradouro || "",
        numero: d.numero || "",
        complemento: d.complemento || "",
        bairro: d.bairro || "",
        cidade: d.cidade || "",
        uf: d.uf || "",
        planoSaudeNome: d.planoSaudeNome || "",
      });
    } catch {
      setErro("Erro ao carregar perfil.");
    } finally {
      setLoading(false);
    }
  }

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  async function buscarCep(cep: string) {
    const n = cep.replace(/\D/g, "");
    if (n.length !== 8) return;
    try {
      const r = await cepApi.buscar(n);
      const d = r.data.data;
      setForm(f => ({ ...f, logradouro: d.logradouro || f.logradouro, bairro: d.bairro || f.bairro, cidade: d.cidade || f.cidade, uf: d.uf || f.uf }));
    } catch {}
  }

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setSalvando(true); setErro(""); setSucesso(false);
    try {
      await pacienteApi.atualizar(userId!, {
        nomeCompleto: form.nomeCompleto,
        email: form.email,
        telefone: form.telefone,
        cep: form.cep,
        logradouro: form.logradouro,
        numero: form.numero,
        complemento: form.complemento,
        bairro: form.bairro,
        cidade: form.cidade,
        uf: form.uf,
      });
      setSucesso(true);
      setTimeout(() => setSucesso(false), 3000);
    } catch (err: unknown) {
      const d = (err as { response?: { data?: { mensagem?: string } } })?.response?.data;
      setErro(d?.mensagem || "Erro ao salvar. Tente novamente.");
    } finally {
      setSalvando(false);
    }
  }

  if (!mounted) return <div className="min-h-screen bg-slate-50 flex items-center justify-center"><div className="text-slate-400 text-sm">Carregando...</div></div>;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button onClick={() => router.push("/dashboard")} className="text-slate-400 hover:text-slate-700">←</button>
            <div>
              <h1 className="font-semibold text-slate-800 text-sm">Meu Perfil</h1>
              <p className="text-xs text-slate-400">Dados pessoais</p>
            </div>
          </div>
          <button onClick={() => { logout(); router.push("/login"); }}
            className="text-xs text-slate-400 hover:text-red-600 transition px-3 py-1.5 rounded-lg hover:bg-red-50">
            Sair
          </button>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6">
        {loading ? (
          <div className="text-center py-16 text-slate-400">Carregando perfil...</div>
        ) : (
          <form onSubmit={salvar}>
            {sucesso && (
              <div className="mb-4 p-3 rounded-lg bg-green-50 border border-green-200 text-green-700 text-sm">
                ✅ Dados atualizados com sucesso!
              </div>
            )}
            {erro && (
              <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{erro}</div>
            )}

            <div className="bg-white rounded-2xl border border-slate-100 p-6 mb-4">
              <h2 className="font-medium text-slate-700 text-sm mb-4">Dados pessoais</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Nome completo</label>
                  <input value={form.nomeCompleto} onChange={e => set("nomeCompleto", e.target.value)}
                    className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">CPF</label>
                    <input value={form.cpf} readOnly
                      className="w-full px-3 py-2.5 text-sm border border-slate-100 rounded-lg bg-slate-50 text-slate-400 cursor-not-allowed" />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">Telefone</label>
                    <input value={form.telefone} onChange={e => set("telefone", formatTel(e.target.value))} maxLength={15}
                      className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Email</label>
                  <input type="email" value={form.email} onChange={e => set("email", e.target.value)}
                    className="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                </div>
                {form.planoSaudeNome && (
                  <div>
                    <label className="block text-xs font-medium text-slate-600 mb-1">Plano de saúde</label>
                    <input value={form.planoSaudeNome} readOnly
                      className="w-full px-3 py-2.5 text-sm border border-slate-100 rounded-lg bg-slate-50 text-slate-500" />
                  </div>
                )}
              </div>
            </div>

            <div className="bg-white rounded-2xl border border-slate-100 p-6 mb-6">
              <h2 className="font-medium text-slate-700 text-sm mb-4">Endereço</h2>
              <div className="space-y-4">
                <div className="flex gap-3">
                  <div className="flex-1">
                    <label className="block text-xs font-medium text-slate-600 mb-1">CEP</label>
                    <input value={form.cep} onChange={e => { set("cep", e.target.value); buscarCep(e.target.value); }} maxLength={9}
                      className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                  <div className="w-24">
                    <label className="block text-xs font-medium text-slate-600 mb-1">Número</label>
                    <input value={form.numero} onChange={e => set("numero", e.target.value)}
                      className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                </div>
                <input value={form.logradouro} onChange={e => set("logradouro", e.target.value)} placeholder="Logradouro"
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                <input value={form.complemento} onChange={e => set("complemento", e.target.value)} placeholder="Complemento (opcional)"
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                <div className="grid grid-cols-3 gap-3">
                  <div className="col-span-2">
                    <input value={form.bairro} onChange={e => set("bairro", e.target.value)} placeholder="Bairro"
                      className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                  </div>
                  <input value={form.uf} onChange={e => set("uf", e.target.value)} maxLength={2} placeholder="UF"
                    className="px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
                </div>
                <input value={form.cidade} onChange={e => set("cidade", e.target.value)} placeholder="Cidade"
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20" />
              </div>
            </div>

            <button type="submit" disabled={salvando}
              className="w-full py-3 bg-primary text-white rounded-xl font-medium text-sm hover:bg-primary-light transition disabled:opacity-60">
              {salvando ? "Salvando..." : "Salvar alterações"}
            </button>
          </form>
        )}
      </main>
    </div>
  );
}
