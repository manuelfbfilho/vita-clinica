package com.vitaclinica.agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║          VITA CLÍNICA — Sistema de Agendamento              ║
 * ║          Multi-Especialidades · API REST · v1.0.0           ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
 * API Docs:   http://localhost:8080/api/v1/v3/api-docs
 *
 * Perfis disponíveis:
 *   --spring.profiles.active=dev    → logs detalhados
 *   --spring.profiles.active=oracle → banco Oracle XE
 */
@SpringBootApplication
@EnableJpaAuditing
public class VitaClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitaClinicaApplication.class, args);
    }
}
