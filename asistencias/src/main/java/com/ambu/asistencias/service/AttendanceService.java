package com.ambu.asistencias.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${app.upload.dir:uploads/photos}")
    private String uploadDir;

    public AttendanceResponse registerAttendance(AttendanceRequest request) {
        return registerAttendance(request, null);
    }

    public AttendanceResponse registerAttendance(AttendanceRequest request, MultipartFile photo) {
        log.info("Registrando asistencia para folio: {} y parque ID: {}", request.getId(), request.getParkId());

        // Buscar SocialServer por id
        SocialServer socialServer = socialServerRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró servidor social por el folio: " + request.getId()));

        // Buscar Park por ID
        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + request.getParkId()));

        // Determinar el tipo de asistencia
        AttendanceType attendanceType = determineAttendanceType(request.getType());

        // Manejar la subida de la foto (si existe)
        String photoPath = "uploads/photos/default-photo.png";
        if (photo == null || photo.isEmpty()) {
            throw new ResourceNotFoundException(
                        "La foto de asistencia es obligatoria.");
        }
            try {
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);

                String originalFilename = StringUtils.cleanPath(photo.getOriginalFilename());
                String extension = "";
                int idx = originalFilename.lastIndexOf('.');
                if (idx > 0) {
                    extension = originalFilename.substring(idx);
                }
                String filename = "attendance-" + UUID.randomUUID() + extension;
                
                Path target = uploadPath.resolve(filename);

                Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                // Save relative path
                photoPath = uploadDir + "/" + filename;
            } catch (IOException e) {
                log.error("Error guardando la foto de asistencia", e);
                throw new RuntimeException("No se pudo guardar la foto de asistencia", e);
            }
        

        // Crear y guardar la asistencia
        Attendance attendance = Attendance.builder()
                .socialServer(socialServer)
                .park(park)
                .timestamp(LocalDateTime.now())
                .type(attendanceType)
                .photoPath(photoPath)
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
                .photoPath(savedAttendance.getPhotoPath())
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

