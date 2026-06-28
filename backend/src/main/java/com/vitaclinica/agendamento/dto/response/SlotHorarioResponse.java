package com.vitaclinica.agendamento.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class SlotHorarioResponse {
    private LocalTime hora;
    private boolean disponivel;
    private String horaFormatada;  // "07:00"

    public static SlotHorarioResponse disponivel(LocalTime hora) {
        return new SlotHorarioResponse(hora, true, hora.toString().substring(0, 5));
    }
    public static SlotHorarioResponse ocupado(LocalTime hora) {
        return new SlotHorarioResponse(hora, false, hora.toString().substring(0, 5));
    }
}
