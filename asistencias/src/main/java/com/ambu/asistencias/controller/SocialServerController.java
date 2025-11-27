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
import com.ambu.asistencias.service.ExcelService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/servidores-sociales")
@RequiredArgsConstructor
@Slf4j
public class SocialServerController {

    private final SocialServerService socialServerService;
    private final ExcelService excelService;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SocialServerResponse> createSocialServer(
            @Valid @RequestPart("data") SocialServerRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        log.info("Solicitud de creación de servidor social recibida");
        SocialServerResponse response = socialServerService.createSocialServer(request, photo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SocialServerResponse> updateSocialServer(
            @PathVariable Long id,
            @Valid @RequestPart("data") SocialServerRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        log.info("Solicitud de actualización de servidor social con ID: {}", id);
        SocialServerResponse response = socialServerService.updateSocialServer(id, request, photo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSocialServer(@PathVariable Long id) {
        log.info("Solicitud de eliminación de servidor social con ID: {}", id);
        socialServerService.deleteSocialServer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> importSocialServers(@RequestPart("file") MultipartFile file) {
        log.info("Solicitud de importación de servidores sociales recibida");
        try {
            excelService.importSocialServers(file);
            return ResponseEntity.ok("Importación exitosa");
        } catch (IOException e) {
            log.error("Error al importar archivo Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo");
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InputStreamResource> exportSocialServers() {
        log.info("Solicitud de exportación de servidores sociales recibida");
        try {
            ByteArrayInputStream in = excelService.exportSocialServers();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=servidores_sociales.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            log.error("Error al exportar archivo Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
