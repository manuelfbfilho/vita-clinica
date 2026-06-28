package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.config.AppProperties;
import com.vitaclinica.agendamento.domain.Agendamento;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoEmailService {

    private final JavaMailSender  mailSender;
    private final TemplateEngine  templateEngine;
    private final AppProperties   props;

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Async
    public void enviarConfirmacao(Agendamento agendamento) {
        try {
            Context ctx = new Context(new Locale("pt", "BR"));
            ctx.setVariable("clinicaNome",    props.getNome());
            ctx.setVariable("pacienteNome",   agendamento.getPaciente().getNomeCompleto());
            ctx.setVariable("data",           agendamento.getDataConsulta().format(FMT_DATA));
            ctx.setVariable("hora",           agendamento.getHoraConsulta().format(FMT_HORA));
            ctx.setVariable("profissional",   agendamento.getProfissional().getNome());
            ctx.setVariable("especialidade",  agendamento.getProfissional().getEspecialidade().getNome());
            ctx.setVariable("tipoAtendimento",agendamento.getTipoAtendimento().getDescricao());
            ctx.setVariable("formaPagamento", agendamento.getFormaPagamento().getDescricao());
            ctx.setVariable("frontendUrl",    props.getFrontendUrl());

            String html = templateEngine.process("email/confirmacao-agendamento", ctx);
            enviar(agendamento.getPaciente().getEmail(),
                    "✅ Consulta confirmada — " + props.getNome(), html);
        } catch (Exception e) {
            log.error("Falha ao enviar email de confirmação para agendamento {}: {}", agendamento.getId(), e.getMessage());
        }
    }

    @Async
    public void enviarCancelamento(Agendamento agendamento) {
        try {
            Context ctx = new Context(new Locale("pt", "BR"));
            ctx.setVariable("clinicaNome",  props.getNome());
            ctx.setVariable("pacienteNome", agendamento.getPaciente().getNomeCompleto());
            ctx.setVariable("data",         agendamento.getDataConsulta().format(FMT_DATA));
            ctx.setVariable("hora",         agendamento.getHoraConsulta().format(FMT_HORA));
            ctx.setVariable("profissional", agendamento.getProfissional().getNome());
            ctx.setVariable("motivo",       agendamento.getCancelamento() != null
                    ? agendamento.getCancelamento().getMotivo() : "—");
            ctx.setVariable("frontendUrl",  props.getFrontendUrl());

            String html = templateEngine.process("email/cancelamento-agendamento", ctx);
            enviar(agendamento.getPaciente().getEmail(),
                    "❌ Consulta cancelada — " + props.getNome(), html);
        } catch (Exception e) {
            log.error("Falha ao enviar email de cancelamento: {}", e.getMessage());
        }
    }

    private void enviar(String para, String assunto, String html) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(props.getEmailRemetente());
        helper.setTo(para);
        helper.setSubject(assunto);
        helper.setText(html, true);
        mailSender.send(msg);
        log.info("Email enviado para {}: {}", para, assunto);
    }
}
