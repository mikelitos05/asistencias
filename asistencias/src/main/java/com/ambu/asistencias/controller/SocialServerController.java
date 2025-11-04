package com.ambu.asistencias.controller;

import com.ambu.asistencias.dto.SocialServerRequest;
import com.ambu.asistencias.dto.SocialServerResponse;
import com.ambu.asistencias.service.SocialServerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servidores-sociales")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo, ajustar en producción
public class SocialServerController {

    private final SocialServerService socialServerService;

    @PostMapping
    public ResponseEntity<SocialServerResponse> createSocialServer(
            @Valid @RequestBody SocialServerRequest request) {
        
        log.info("Solicitud de creación de servidor social recibida");
        SocialServerResponse response = socialServerService.createSocialServer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

