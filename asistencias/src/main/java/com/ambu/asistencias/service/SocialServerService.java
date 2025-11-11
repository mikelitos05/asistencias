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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialServerService {

    private final SocialServerRepository socialServerRepository;
    private final ParkRepository parkRepository;

    public List<SocialServerResponse> getAllSocialServers() {
        List<SocialServer> socialServers = socialServerRepository.findAll();
        return socialServers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SocialServerResponse getSocialServerById(Long id) {
        SocialServer socialServer = socialServerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un servidor social con el ID: " + id));
        return mapToResponse(socialServer);
    }

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

        return mapToResponse(savedSocialServer, "Servidor social creado exitosamente");
    }

    public SocialServerResponse updateSocialServer(Long id, SocialServerRequest request) {
        log.info("Actualizando servidor social con ID: {}", id);

        SocialServer socialServer = socialServerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un servidor social con el ID: " + id));

        // Validar que el email no exista en otro servidor social
        socialServerRepository.findByEmail(request.getEmail())
                .ifPresent(existingServer -> {
                    if (!existingServer.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException(
                                "Ya existe un servidor social con el correo: " + request.getEmail());
                    }
                });

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

        // Actualizar el servidor social
        socialServer.setEmail(request.getEmail());
        socialServer.setName(request.getName());
        socialServer.setPark(park);
        socialServer.setSchool(request.getSchool());
        socialServer.setProgram(request.getProgram());
        socialServer.setStartTime(request.getStartTime());
        socialServer.setEndTime(request.getEndTime());
        socialServer.setTotalHoursRequired(request.getTotalHours());

        SocialServer updatedSocialServer = socialServerRepository.save(socialServer);
        log.info("Servidor social actualizado exitosamente con ID: {}", updatedSocialServer.getId());

        return mapToResponse(updatedSocialServer, "Servidor social actualizado exitosamente");
    }

    public void deleteSocialServer(Long id) {
        log.info("Eliminando servidor social con ID: {}", id);

        SocialServer socialServer = socialServerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un servidor social con el ID: " + id));

        socialServerRepository.delete(socialServer);
        log.info("Servidor social eliminado exitosamente con ID: {}", id);
    }

    private SocialServerResponse mapToResponse(SocialServer socialServer) {
        return mapToResponse(socialServer, null);
    }

    private SocialServerResponse mapToResponse(SocialServer socialServer, String message) {
        return SocialServerResponse.builder()
                .id(socialServer.getId())
                .email(socialServer.getEmail())
                .name(socialServer.getName())
                .parkId(socialServer.getPark().getId())
                .parkName(socialServer.getPark().getParkName())
                .school(socialServer.getSchool())
                .program(socialServer.getProgram())
                .startTime(socialServer.getStartTime())
                .endTime(socialServer.getEndTime())
                .totalHoursRequired(socialServer.getTotalHoursRequired())
                .message(message)
                .build();
    }
}

