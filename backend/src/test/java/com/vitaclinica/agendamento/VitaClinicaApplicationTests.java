package com.vitaclinica.agendamento;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.mail.host=localhost",
    "spring.mail.port=25",
    "vita.clinica.email-remetente=test@test.com",
    "vita.clinica.nome=Vita Clinica Test",
    "vita.clinica.frontend-url=http://localhost:3000",
    "jwt.secret=TestSecretKeyParaJWTVitaClinica2025",
    "jwt.expiration=86400000"
})
class VitaClinicaApplicationTests {
    @Test
    void contextLoads() {}
}
