package com.ambu.asistencias.controller;

import com.ambu.asistencias.config.JwtService;
import com.ambu.asistencias.config.CustomUserDetails;
import com.ambu.asistencias.dto.LoginRequest;
import com.ambu.asistencias.dto.LoginResponse;
import com.ambu.asistencias.model.User;
import com.ambu.asistencias.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Intento de login para email: {}", request.getEmail());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            
            if (userDetails instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                User user = customUserDetails.getUser();

                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("role", user.getRole().name());

                String token = jwtService.generateToken(userDetails, extraClaims);

                LoginResponse response = LoginResponse.builder()
                        .token(token)
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole())
                        .message("Login exitoso")
                        .build();

                log.info("Login exitoso para usuario: {}", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Error al obtener detalles del usuario");
            }

        } catch (Exception e) {
            log.error("Error en login para email: {}", request.getEmail(), e);
            throw new UsernameNotFoundException("Credenciales inválidas");
        }
    }
}

