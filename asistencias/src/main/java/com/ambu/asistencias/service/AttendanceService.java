package com.ambu.asistencias.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SocialServerRepository socialServerRepository;
    private final ParkRepository parkRepository;

    public AttendanceResponse registerAttendance(AttendanceRequest request) {
        log.info("Registrando asistencia para email: {} y parque ID: {}", request.getEmail(), request.getParkId());

        // Buscar SocialServer por email
        SocialServer socialServer = socialServerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un servidor social con el correo: " + request.getEmail()));

        // Buscar Park por ID
        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + request.getParkId()));

        // Validar que el SocialServer pertenezca al Park especificado
        if (!socialServer.getPark().getId().equals(park.getId())) {
            throw new IllegalArgumentException(
                    "El servidor social con correo " + request.getEmail() + 
                    " no pertenece al parque especificado");
        }

        // Determinar el tipo de asistencia
        AttendanceType attendanceType = determineAttendanceType(request, socialServer);

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

    /**
     * Determina el tipo de asistencia basándose en la última asistencia del servidor social
     * Si no hay asistencias previas o la última es CHECK_OUT, se registra CHECK_IN
     * Si la última es CHECK_IN, se registra CHECK_OUT
     */
    private AttendanceType determineAttendanceType(AttendanceRequest request, SocialServer socialServer) {
        // Si se especifica el tipo en el request, validarlo y usarlo
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                return AttendanceType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Tipo de asistencia inválido: " + request.getType() + 
                        ". Debe ser CHECK_IN o CHECK_OUT");
            }
        }

        // Si no se especifica, determinar automáticamente basándose en la última asistencia
        List<Attendance> recentAttendances = attendanceRepository
                .findBySocialServerOrderByTimestampDesc(socialServer);

        if (recentAttendances.isEmpty()) {
            // Primera asistencia, siempre es CHECK_IN
            return AttendanceType.CHECK_IN;
        }

        // Obtener la última asistencia
        Attendance lastAttendance = recentAttendances.get(0);
        // Si la última fue CHECK_IN, la siguiente es CHECK_OUT, y viceversa
        return lastAttendance.getType() == AttendanceType.CHECK_IN 
                ? AttendanceType.CHECK_OUT 
                : AttendanceType.CHECK_IN;
    }
}

