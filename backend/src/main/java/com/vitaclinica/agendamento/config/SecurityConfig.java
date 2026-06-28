package com.vitaclinica.agendamento.config;

import com.vitaclinica.agendamento.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${vita.clinica.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────
    // PASSWORD ENCODER — BCrypt força 12
    // Bean aqui para evitar circular dependency com DataSeeder
    // ─────────────────────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─────────────────────────────────────────────────────────────
    // SECURITY FILTER CHAIN
    // ─────────────────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ── ROTAS PÚBLICAS ──────────────────────────────
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/pacientes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profissionais").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profissionais/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/especialidades").permitAll()
                        .requestMatchers(HttpMethod.GET, "/planos-saude").permitAll()
                        .requestMatchers(HttpMethod.GET, "/agendamentos/horarios-disponiveis").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cep/**").permitAll()
                        // ── SWAGGER UI (dev) ────────────────────────────
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // ── APENAS ADMIN ────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/funcionarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/funcionarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/profissionais").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/profissionais/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/profissionais/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/clinica").hasRole("ADMIN")
                        .requestMatchers("/planos-saude/**").hasRole("ADMIN")
                        // ── FUNCIONARIO E ADMIN ─────────────────────────
                        .requestMatchers(HttpMethod.GET, "/pacientes").hasAnyRole("FUNCIONARIO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/funcionarios").hasAnyRole("FUNCIONARIO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/agendamentos").hasAnyRole("FUNCIONARIO", "ADMIN")
                        .requestMatchers("/indisponibilidades/**").hasAnyRole("FUNCIONARIO", "ADMIN")
                        // ── QUALQUER AUTENTICADO ────────────────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // CORS — permite requisições do frontend Vercel e localhost
    // ─────────────────────────────────────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                frontendUrl,
                "http://localhost:3000",
                "http://localhost:3001"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
