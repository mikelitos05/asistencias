package com.ambu.asistencias.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.coobird.thumbnailator.Thumbnails;

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
    private final AppConfigService appConfigService;

    @Value("${app.upload.dir:uploads/photos}")
    private String uploadDir;

    public AttendanceResponse registerAttendance(AttendanceRequest request) {
        return registerAttendance(request, null);
    }

    public List<AttendanceResponse> getAllAttendances() {
        List<Attendance> attendances = attendanceRepository.findAll();
        return attendances.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró una asistencia con el ID: " + id));
        return mapToResponse(attendance);
    }

    public List<AttendanceResponse> getAttendancesBySocialServerId(Long socialServerId) {
        log.info("Obteniendo asistencias para servidor social con ID: {}", socialServerId);

        SocialServer socialServer = socialServerRepository.findById(socialServerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un servidor social con el ID: " + socialServerId));

        List<Attendance> attendances = attendanceRepository.findBySocialServerOrderByTimestampDesc(socialServer);
        return attendances.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

            long maxSizeBytes = appConfigService.getMaxPhotoSizeMB() * 1024L * 1024L;
            if (photo.getSize() > maxSizeBytes) {
                log.info("La foto excede el límite de {}MB. Redimensionando...", appConfigService.getMaxPhotoSizeMB());
                Thumbnails.of(photo.getInputStream())
                        .size(2048, 2048)
                        .outputQuality(0.8)
                        .toFile(target.toFile());
            } else {
                Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            }

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

        return mapToResponse(savedAttendance);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        String message = attendance.getType() == AttendanceType.CHECK_IN
                ? "Entrada registrada exitosamente"
                : "Salida registrada exitosamente";

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .email(attendance.getSocialServer().getEmail())
                .socialServerName(attendance.getSocialServer().getName())
                .parkName(attendance.getPark().getParkName())
                .timestamp(attendance.getTimestamp())
                .type(attendance.getType().name())
                .message(message)
                .photoPath(attendance.getPhotoPath())

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
