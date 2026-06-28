package com.vitaclinica.agendamento.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.LocalTime;

@Data
@Component
@ConfigurationProperties(prefix = "vita.clinica")
public class AppProperties {

    private String nome = "Vita Clínica";
    private String emailRemetente = "contato@vitaclinica.com.br";
    private String frontendUrl = "http://localhost:3000";

    private Horario horario = new Horario();
    private Consulta consulta = new Consulta();

    @Data
    public static class Horario {
        private Periodo semana = new Periodo(LocalTime.of(7, 0), LocalTime.of(20, 0));
        private Periodo sabado  = new Periodo(LocalTime.of(8, 0), LocalTime.of(13, 0));

        @Data
        public static class Periodo {
            private LocalTime inicio;
            private LocalTime fim;
            public Periodo(LocalTime inicio, LocalTime fim) {
                this.inicio = inicio;
                this.fim = fim;
            }
            public Periodo() {}
        }
    }

    @Data
    public static class Consulta {
        private int duracaoMinutos = 30;
        private int intervaloMinutos = 10;

        /** Duração total de cada bloco (consulta + intervalo) */
        public int blocoMinutos() {
            return duracaoMinutos + intervaloMinutos;
        }
    }
}
