export type Role = "ROLE_PACIENTE" | "ROLE_FUNCIONARIO" | "ROLE_ADMIN";
export interface Profissional { id: number; nome: string; crm: string; especialidadeId: number; especialidadeNome: string; valorConsulta: number; }
export interface Especialidade { id: number; nome: string; }
export interface PlanoSaude { id: number; nome: string; }
export interface SlotHorario { hora: string; disponivel: boolean; horaFormatada: string; }
export type StatusAgendamento = "AGENDADO" | "CONFIRMADO" | "CANCELADO" | "CONCLUIDO" | "NAO_COMPARECEU";
export interface Agendamento { id: number; pacienteNome: string; profissionalNome: string; especialidadeNome: string; dataConsulta: string; horaConsulta: string; tipoAtendimento: string; tipoConsulta: string; status: StatusAgendamento; formaPagamento: string; motivoCancelamento?: string; }
export interface ApiResponse<T> { success: boolean; data: T; mensagem?: string; erro?: string; campos?: Record<string, string>; }
