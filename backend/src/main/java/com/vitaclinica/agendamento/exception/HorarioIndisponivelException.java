package com.vitaclinica.agendamento.exception;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Lançada quando o horário solicitado para agendamento não está disponível (HTTP 409) */
public class HorarioIndisponivelException extends RuntimeException {

    public HorarioIndisponivelException(LocalDate data, LocalTime hora, String motivo) {
        super(String.format(
            "O horário %s do dia %s não está disponível. %s",
            hora.format(DateTimeFormatter.ofPattern("HH:mm")),
            data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            motivo
        ));
    }
}
