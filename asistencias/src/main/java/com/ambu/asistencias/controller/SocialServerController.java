package com.ambu.asistencias.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<SocialServerResponse> createSocialServer(
            @Valid @RequestBody SocialServerRequest request) {
        
        log.info("Solicitud de creación de servidor social recibida");
        SocialServerResponse response = socialServerService.createSocialServer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

