package com.ambu.asistencias.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ambu.asistencias.dto.SocialServerRequest;
import com.ambu.asistencias.dto.SocialServerResponse;
import com.ambu.asistencias.service.SocialServerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/servidores-sociales")
@RequiredArgsConstructor
@Slf4j
public class SocialServerController {

    private final SocialServerService socialServerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<SocialServerResponse>> getAllSocialServers() {
        log.info("Solicitud de listado de servidores sociales recibida");
        List<SocialServerResponse> response = socialServerService.getAllSocialServers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SocialServerResponse> getSocialServerById(@PathVariable Long id) {
        log.info("Solicitud de servidor social con ID: {}", id);
        SocialServerResponse response = socialServerService.getSocialServerById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SocialServerResponse> createSocialServer(
            @Valid @RequestBody SocialServerRequest request) {
        
        log.info("Solicitud de creación de servidor social recibida");
        SocialServerResponse response = socialServerService.createSocialServer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SocialServerResponse> updateSocialServer(
            @PathVariable Long id,
            @Valid @RequestBody SocialServerRequest request) {
        
        log.info("Solicitud de actualización de servidor social con ID: {}", id);
        SocialServerResponse response = socialServerService.updateSocialServer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSocialServer(@PathVariable Long id) {
        log.info("Solicitud de eliminación de servidor social con ID: {}", id);
        socialServerService.deleteSocialServer(id);
        return ResponseEntity.noContent().build();
    }
}

