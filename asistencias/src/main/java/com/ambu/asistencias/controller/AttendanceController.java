package com.ambu.asistencias.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ambu.asistencias.dto.AttendanceRequest;
import com.ambu.asistencias.dto.AttendanceResponse;
import com.ambu.asistencias.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/asistencia")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo, ajustar en producción
public class AttendanceController {

    private final AttendanceService attendanceService;

      @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceResponse> registerAttendance(
            @Valid @ModelAttribute AttendanceRequest request,
            @RequestPart(value = "photo", required = true) MultipartFile photo) {

        log.info("Solicitud de registro de asistencia recibida para id={} parkId={}", request.getId(), request.getParkId());

        AttendanceResponse response = attendanceService.registerAttendance(request, photo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
