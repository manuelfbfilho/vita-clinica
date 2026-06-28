"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";

export default function AdminPage() {
  const router = useRouter();
  const { isAdmin, userId } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => { setMounted(true); }, []);
  useEffect(() => {
    if (!mounted) return;
    if (!userId || !isAdmin()) { router.push("/dashboard"); return; }
  }, [mounted, userId]);

  if (!mounted) return <div className="min-h-screen bg-slate-50 flex items-center justify-center"><div className="text-slate-400 text-sm">Carregando...</div></div>;

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-100 shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push("/dashboard")} className="text-slate-400 hover:text-slate-700">←</button>
          <h1 className="font-semibold text-slate-800 text-sm">Administração</h1>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-4 py-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {[
            { emoji: "👨‍⚕️", title: "Profissionais", desc: "Gerenciar médicos e especialidades", action: "Em breve" },
            { emoji: "👤", title: "Funcionários", desc: "Cadastrar novos funcionários", action: "Em breve" },
            { emoji: "🏥", title: "Dados da Clínica", desc: "Nome, endereço e contatos", action: "Em breve" },
            { emoji: "💳", title: "Planos de Saúde", desc: "Convênios aceitos", action: "Em breve" },
            { emoji: "🚫", title: "Indisponibilidades", desc: "Bloquear horários e datas", action: "Em breve" },
          ].map(item => (
            <div key={item.title} className="bg-white rounded-xl border border-slate-100 p-5">
              <div className="text-2xl mb-2">{item.emoji}</div>
              <div className="font-semibold text-slate-800 text-sm">{item.title}</div>
              <div className="text-xs text-slate-400 mt-1 mb-3">{item.desc}</div>
              <span className="text-xs bg-amber-50 text-amber-600 px-2 py-1 rounded-full border border-amber-200">
                {item.action}
              </span>
            </div>
          ))}
        </div>
        <p className="text-center text-xs text-slate-400 mt-6">
          Funcionalidades administrativas via{" "}
          <a href={`${process.env.NEXT_PUBLIC_API_URL?.replace("/api/v1","")}/api/v1/swagger-ui.html`}
            target="_blank" className="text-primary hover:underline">
            Swagger UI
          </a>
        </p>
      </main>
    </div>
  );
}
