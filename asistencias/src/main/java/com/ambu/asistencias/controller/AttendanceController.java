package com.ambu.asistencias.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ambu.asistencias.dto.AttendanceResponse;
import com.ambu.asistencias.dto.AttendanceRequest;
import com.ambu.asistencias.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/asistencias")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendances() {
        log.info("Solicitud de listado de asistencias recibida");
        List<AttendanceResponse> response = attendanceService.getAllAttendances();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable Long id) {
        log.info("Solicitud de asistencia con ID: {}", id);
        AttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/servidor-social/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesBySocialServerId(@PathVariable Long id) {
        log.info("Solicitud de asistencias para servidor social con ID: {}", id);
        List<AttendanceResponse> response = attendanceService.getAttendancesBySocialServerId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceResponse> registerAttendance(
            @Valid @ModelAttribute AttendanceRequest request,
            @RequestPart(value = "photo", required = true) MultipartFile photo) {

        log.info("Solicitud de registro de asistencia recibida para id={} parkId={}", request.getId(), request.getParkId());

        AttendanceResponse response = attendanceService.registerAttendance(request, photo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
