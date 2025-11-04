package com.ambu.asistencias.controller;

import com.ambu.asistencias.dto.AttendanceRequest;
import com.ambu.asistencias.dto.AttendanceResponse;
import com.ambu.asistencias.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo, ajustar en producción
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/asistencia")
    public ResponseEntity<AttendanceResponse> registerAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        
        log.info("Solicitud de registro de asistencia recibida");
        AttendanceResponse response = attendanceService.registerAttendance(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
