package com.ambu.asistencias.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${api.prefix:/api}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - sin autenticación
                        .requestMatchers(apiPrefix + "/auth/login").permitAll()
                        .requestMatchers("POST", apiPrefix + "/asistencias").permitAll()
                        .requestMatchers("GET", apiPrefix + "/parques").permitAll()
                        .requestMatchers("GET", apiPrefix + "/parques/{id}").permitAll()

                        // Endpoints de parques - ADMIN y SUPER_ADMIN pueden crear, actualizar y
                        // eliminar
                        .requestMatchers("POST", apiPrefix + "/parques").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("PUT", apiPrefix + "/parques/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("DELETE", apiPrefix + "/parques/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Endpoints de programas - solo ADMIN y SUPER_ADMIN
                        .requestMatchers(apiPrefix + "/programs/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Endpoints de servidores sociales - solo ADMIN y SUPER_ADMIN
                        .requestMatchers(apiPrefix + "/servidores-sociales/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Endpoints de asistencias - GET solo para ADMIN y SUPER_ADMIN, POST público
                        .requestMatchers("GET", apiPrefix + "/asistencias").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("GET", apiPrefix + "/asistencias/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Endpoints de administración - solo ADMIN y SUPER_ADMIN
                        .requestMatchers(apiPrefix + "/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Todas las demás solicitudes requieren autenticación
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
