package com.ambu.asistencias.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ambu.asistencias.dto.AttendanceRequest;
import com.ambu.asistencias.dto.AttendanceResponse;
import com.ambu.asistencias.exception.ResourceNotFoundException;
import com.ambu.asistencias.model.Attendance;
import com.ambu.asistencias.model.Attendance.AttendanceType;
import com.ambu.asistencias.model.Park;
import com.ambu.asistencias.model.SocialServer;
import com.ambu.asistencias.repository.AttendanceRepository;
import com.ambu.asistencias.repository.ParkRepository;
import com.ambu.asistencias.repository.SocialServerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SocialServerRepository socialServerRepository;
    private final ParkRepository parkRepository;

    public AttendanceResponse registerAttendance(AttendanceRequest request) {
        log.info("Registrando asistencia para folio: {} y parque ID: {}", request.getId(), request.getParkId());

        // Buscar SocialServer por email
        SocialServer socialServer = socialServerRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró servidor social por el folio: " + request.getId()));

        // Buscar Park por ID
        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + request.getParkId()));


        // Determinar el tipo de asistencia
        AttendanceType attendanceType = determineAttendanceType(request.getType());

        // Crear y guardar la asistencia
        Attendance attendance = Attendance.builder()
                .socialServer(socialServer)
                .park(park)
                .timestamp(LocalDateTime.now())
                .type(attendanceType)
                .photoPath("default-photo-path") // TODO: Implementar subida de fotos si es necesario
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Asistencia registrada exitosamente con ID: {}", savedAttendance.getId());

        // Construir respuesta
        String message = attendanceType == AttendanceType.CHECK_IN 
                ? "Entrada registrada exitosamente" 
                : "Salida registrada exitosamente";

        return AttendanceResponse.builder()
                .id(savedAttendance.getId())
                .email(socialServer.getEmail())
                .socialServerName(socialServer.getName())
                .parkName(park.getParkName())
                .timestamp(savedAttendance.getTimestamp())
                .type(savedAttendance.getType().name())
                .message(message)
                .build();
    }

    private AttendanceType determineAttendanceType(String type) {

        if ("CHECK_IN".equals(type)) {
            return AttendanceType.CHECK_IN;
        } else if ("CHECK_OUT".equals(type)) {
            return AttendanceType.CHECK_OUT;
        } else {
            throw new ResourceNotFoundException(
                        "Ponga un tipo de entrada valido: " + type);
        }
    }


}

