package com.vitaclinica.agendamento.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vitaClinicaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vita Clínica — API de Agendamento")
                        .description("""
                            ## Sistema de Agendamento de Consultas Médicas
                            
                            **Vita Clínica Multi-Especialidades** — API REST completa para
                            agendamento de consultas, gerenciamento de pacientes, profissionais
                            e funcionários.
                            
                            ### Autenticação
                            1. Use `POST /auth/login` com CPF + senha
                            2. Copie o token JWT retornado
                            3. Clique em **Authorize** e insira: `Bearer {seu-token}`
                            
                            ### Perfis de acesso
                            - **PACIENTE**: ver e gerenciar próprios agendamentos
                            - **FUNCIONARIO**: acesso operacional completo
                            - **ADMIN**: acesso total incluindo gestão de usuários
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vita Clínica")
                                .email("contato@vitaclinica.com.br"))
                        .license(new License()
                                .name("Privado — uso interno")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Informe o token JWT retornado pelo endpoint /auth/login")));
    }
}
