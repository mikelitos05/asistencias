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

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialServerService {

    private final SocialServerRepository socialServerRepository;
    private final ParkRepository parkRepository;
    private final com.ambu.asistencias.repository.ScheduleRepository scheduleRepository;
    private final com.ambu.asistencias.repository.ProgramRepository programRepository;

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

        // Buscar el Schedule por ID
        com.ambu.asistencias.model.Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un horario con el ID: " + request.getScheduleId()));

        // Obtener el programa del horario
        com.ambu.asistencias.model.Program program = schedule.getProgram();

        // Validar capacidad
        if (program.getCurrentCapacity() <= 0) {
            throw new IllegalStateException("El programa " + program.getName() + " no tiene capacidad disponible.");
        }

        // Decrementar capacidad
        program.setCurrentCapacity(program.getCurrentCapacity() - 1);
        programRepository.save(program);

        // Crear el SocialServer
        SocialServer socialServer = SocialServer.builder()
                .email(request.getEmail())
                .name(request.getName())
                .park(park)
                .school(request.getSchool())
                .schedule(schedule)
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

        // Buscar el Schedule por ID
        com.ambu.asistencias.model.Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un horario con el ID: " + request.getScheduleId()));

        // Nota: Si se cambia de programa, se debería ajustar la capacidad de ambos
        // programas (viejo y nuevo).
        // Por simplicidad en esta iteración, asumiremos que solo se actualiza la info
        // del servidor,
        // pero si cambia el schedule, deberíamos manejar la capacidad.
        // Implementación básica:
        if (!socialServer.getSchedule().getId().equals(schedule.getId())) {
            // Lógica de cambio de capacidad si cambia el programa
            com.ambu.asistencias.model.Program oldProgram = socialServer.getSchedule().getProgram();
            com.ambu.asistencias.model.Program newProgram = schedule.getProgram();

            if (!oldProgram.getId().equals(newProgram.getId())) {
                oldProgram.setCurrentCapacity(oldProgram.getCurrentCapacity() + 1);
                programRepository.save(oldProgram);

                if (newProgram.getCurrentCapacity() <= 0) {
                    throw new IllegalStateException("El nuevo programa no tiene capacidad.");
                }
                newProgram.setCurrentCapacity(newProgram.getCurrentCapacity() - 1);
                programRepository.save(newProgram);
            }
        }

        // Actualizar el servidor social
        socialServer.setEmail(request.getEmail());
        socialServer.setName(request.getName());
        socialServer.setPark(park);
        socialServer.setSchool(request.getSchool());
        socialServer.setSchedule(schedule);
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
        // Handle null schedule for legacy social servers
        Long programId = null;
        String programName = null;
        Long scheduleId = null;
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (socialServer.getSchedule() != null) {
            scheduleId = socialServer.getSchedule().getId();
            startTime = socialServer.getSchedule().getStartTime();
            endTime = socialServer.getSchedule().getEndTime();

            if (socialServer.getSchedule().getProgram() != null) {
                programId = socialServer.getSchedule().getProgram().getId();
                programName = socialServer.getSchedule().getProgram().getName();
            }
        }

        return SocialServerResponse.builder()
                .id(socialServer.getId())
                .email(socialServer.getEmail())
                .name(socialServer.getName())
                .parkId(socialServer.getPark().getId())
                .parkName(socialServer.getPark().getParkName())
                .school(socialServer.getSchool())
                .programId(programId)
                .program(programName)
                .scheduleId(scheduleId)
                .startTime(startTime)
                .endTime(endTime)
                .totalHoursRequired(socialServer.getTotalHoursRequired())
                .message(message)
                .build();
    }
}
