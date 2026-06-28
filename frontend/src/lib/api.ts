import axios from "axios";
const api = axios.create({ baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1" });
api.interceptors.request.use((c) => {
  if (typeof window !== "undefined") {
    try { const s = JSON.parse(localStorage.getItem("vita-auth") || "{}"); if (s?.state?.token) c.headers.Authorization = `Bearer ${s.state.token}`; } catch {}
  }
  return c;
});
api.interceptors.response.use(r => r, e => {
  if (e.response?.status === 401 && typeof window !== "undefined") { localStorage.removeItem("vita-auth"); window.location.href = "/login"; }
  return Promise.reject(e);
});
export default api;
export const profissionalApi = { listar: (espId?: number) => api.get("/profissionais", { params: { especialidadeId: espId } }) };
export const especialidadeApi = { listar: () => api.get("/especialidades") };
export const planoApi = { listar: () => api.get("/planos-saude") };
export const cepApi = { buscar: (cep: string) => api.get(`/cep/${cep}`) };
export const agendamentoApi = {
  slots: (profissionalId: number, data: string) => api.get("/agendamentos/horarios-disponiveis", { params: { profissionalId, data } }),
  criar: (d: unknown) => api.post("/agendamentos", d),
  meus: (page = 0) => api.get("/agendamentos/meus", { params: { page, size: 10 } }),
  listar: (p?: Record<string, unknown>) => api.get("/agendamentos", { params: p }),
  cancelar: (id: number, motivo: string) => api.patch(`/agendamentos/${id}/cancelar`, { motivo }),
  agendaDia: (data?: string) => api.get("/agendamentos/agenda-dia", { params: { data } }),
};
export const pacienteApi = { cadastrar: (d: unknown) => api.post("/pacientes", d), buscar: (id: number) => api.get(`/pacientes/${id}`), atualizar: (id: number, d: unknown) => api.put(`/pacientes/${id}`, d) };
export const authApi = { login: (cpf: string, senha: string) => api.post("/auth/login", { cpf, senha }) };
