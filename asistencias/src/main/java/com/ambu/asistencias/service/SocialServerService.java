package com.ambu.asistencias.service;

import com.ambu.asistencias.dto.SocialServerRequest;
import com.ambu.asistencias.dto.SocialServerResponse;
import com.ambu.asistencias.exception.ResourceAlreadyExistsException;
import com.ambu.asistencias.exception.ResourceNotFoundException;
import com.ambu.asistencias.model.Park;
import com.ambu.asistencias.model.SocialServer;
import com.ambu.asistencias.repository.ParkRepository;
import com.ambu.asistencias.repository.SocialServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialServerService {

    private final SocialServerRepository socialServerRepository;
    private final ParkRepository parkRepository;

    public SocialServerResponse createSocialServer(SocialServerRequest request) {
        log.info("Creando nuevo servidor social con email: {}", request.getEmail());

        // Validar que el email no exista
        if (socialServerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Ya existe un servidor social con el correo: " + request.getEmail());
        }

        // Buscar el Park por ID
        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + request.getParkId()));

        // Validar que startTime sea anterior a endTime
        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }

        // Crear el SocialServer
        SocialServer socialServer = SocialServer.builder()
                .email(request.getEmail())
                .name(request.getName())
                .park(park)
                .school(request.getSchool())
                .program(request.getProgram())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalHoursRequired(request.getTotalHours())
                .build();

        SocialServer savedSocialServer = socialServerRepository.save(socialServer);
        log.info("Servidor social creado exitosamente con ID: {}", savedSocialServer.getId());

        // Construir respuesta
        return SocialServerResponse.builder()
                .id(savedSocialServer.getId())
                .email(savedSocialServer.getEmail())
                .name(savedSocialServer.getName())
                .parkId(savedSocialServer.getPark().getId())
                .parkName(savedSocialServer.getPark().getParkName())
                .school(savedSocialServer.getSchool())
                .program(savedSocialServer.getProgram())
                .startTime(savedSocialServer.getStartTime())
                .endTime(savedSocialServer.getEndTime())
                .totalHoursRequired(savedSocialServer.getTotalHoursRequired())
                .message("Servidor social creado exitosamente")
                .build();
    }
}

