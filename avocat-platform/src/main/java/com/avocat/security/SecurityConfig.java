package com.avocat.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver CSRF (nécessaire pour les APIs REST)
                .csrf(csrf -> csrf.disable())

                // 2. Configurer le CORS pour autoriser Angular (localhost:4200)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:4200"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                // 3. Gestion des sessions (Stateless car on utilise JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. LES AUTORISATIONS (C'est ici que se trouve ta ligne)
                .authorizeHttpRequests(auth -> auth
                        // ICI : On autorise tout le monde à appeler le login et le register sans être connecté
                        .requestMatchers("/api/auth/**").permitAll()

                        // Tout le reste nécessite d'être authentifié
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}