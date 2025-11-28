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

import com.ambu.asistencias.model.EmergencyContact;
import com.ambu.asistencias.model.Period;

import com.ambu.asistencias.repository.PeriodRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialServerService {

    private final SocialServerRepository socialServerRepository;
    private final ParkRepository parkRepository;
    private final com.ambu.asistencias.repository.ScheduleRepository scheduleRepository;
    private final com.ambu.asistencias.repository.ProgramRepository programRepository;
    private final PeriodRepository periodRepository;
    private final AppConfigService appConfigService;

    @Value("${app.upload.dir:uploads/photos}")
    private String uploadDir;

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

    public SocialServerResponse createSocialServer(SocialServerRequest request, MultipartFile photo) {
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

        // Determine status
        SocialServer.Status status = SocialServer.Status
                .valueOf(request.getStatus() != null ? request.getStatus() : "ACTIVO");

        // Only consume capacity if status is ACTIVO
        if (status == SocialServer.Status.ACTIVO) {
            // Validar capacidad del horario
            if (schedule.getCurrentCapacity() <= 0) {
                throw new IllegalStateException("El horario seleccionado no tiene capacidad disponible.");
            }

            // Decrementar capacidad del horario
            schedule.setCurrentCapacity(schedule.getCurrentCapacity() - 1);
            scheduleRepository.save(schedule);
        }

        // Manejar foto
        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            photoPath = savePhoto(photo);
        }

        // Crear EmergencyContact
        EmergencyContact emergencyContact = null;
        if (request.getTutorName() != null && request.getTutorPhone() != null) {
            emergencyContact = EmergencyContact.builder()
                    .tutorName(request.getTutorName())
                    .tutorPhone(request.getTutorPhone())
                    .build();
        }

        // Buscar Period
        Period period = null;
        if (request.getPeriodId() != null) {
            period = periodRepository.findById(request.getPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró periodo con ID: " + request.getPeriodId()));
        }

        // Crear el SocialServer
        SocialServer socialServer = SocialServer.builder()
                .email(request.getEmail())
                .name(request.getName())
                .park(park)
                .school(request.getSchool())
                .schedule(schedule)
                .totalHoursRequired(request.getTotalHours())
                .enrollmentDate(request.getEnrollmentDate() != null ? request.getEnrollmentDate() : LocalDate.now())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .photoPath(photoPath)
                .badge(request.getBadge())
                .vest(request.getVest())
                .emergencyContact(emergencyContact)
                .cellPhone(request.getCellPhone())
                .bloodType(request.getBloodType() != null ? SocialServer.BloodType.valueOf(request.getBloodType())
                        : SocialServer.BloodType.DESCONOCE)
                .allergy(request.getAllergy())
                .birthDate(request.getBirthDate())
                .major(request.getMajor())
                .period(period)
                .socialServerType(SocialServer.SocialServerType.valueOf(request.getSocialServerType()))
                .generalInductionDate(request.getGeneralInductionDate())
                .acceptanceLetterId(request.getAcceptanceLetterId())
                .completionLetterId(request.getCompletionLetterId())
                .build();

        SocialServer savedSocialServer = socialServerRepository.save(socialServer);
        log.info("Servidor social creado exitosamente con ID: {}", savedSocialServer.getId());

        return mapToResponse(savedSocialServer, "Servidor social creado exitosamente");
    }

    private String savePhoto(MultipartFile photo) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFilename = StringUtils.cleanPath(photo.getOriginalFilename());
            String extension = "";
            int idx = originalFilename.lastIndexOf('.');
            if (idx > 0) {
                extension = originalFilename.substring(idx);
            }
            String filename = "social-server-" + UUID.randomUUID() + extension;

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

            return "uploads/photos/" + filename;
        } catch (IOException e) {
            log.error("Error guardando la foto", e);
            throw new RuntimeException("No se pudo guardar la foto", e);
        }
    }

    public SocialServerResponse updateSocialServer(Long id, SocialServerRequest request, MultipartFile photo) {
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
        com.ambu.asistencias.model.Schedule newSchedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un horario con el ID: " + request.getScheduleId()));

        SocialServer.Status oldStatus = socialServer.getStatus();
        SocialServer.Status newStatus = request.getStatus() != null ? SocialServer.Status.valueOf(request.getStatus())
                : oldStatus;

        com.ambu.asistencias.model.Schedule oldSchedule = socialServer.getSchedule();
        boolean scheduleChanged = !oldSchedule.getId().equals(newSchedule.getId());

        // Logic for capacity management considering status and schedule changes

        // Case 1: Schedule Changed
        if (scheduleChanged) {
            // If user was ACTIVO in old schedule, release capacity there
            if (oldStatus == SocialServer.Status.ACTIVO) {
                oldSchedule.setCurrentCapacity(oldSchedule.getCurrentCapacity() + 1);
                scheduleRepository.save(oldSchedule);
            }

            // If user is becoming/staying ACTIVO in new schedule, consume capacity there
            if (newStatus == SocialServer.Status.ACTIVO) {
                if (newSchedule.getCurrentCapacity() <= 0) {
                    throw new IllegalStateException("El nuevo horario no tiene capacidad disponible.");
                }
                newSchedule.setCurrentCapacity(newSchedule.getCurrentCapacity() - 1);
                scheduleRepository.save(newSchedule);
            }
        }
        // Case 2: Schedule NOT Changed, but Status Changed
        else {
            if (oldStatus == SocialServer.Status.ACTIVO && newStatus == SocialServer.Status.INACTIVO) {
                // Release capacity
                oldSchedule.setCurrentCapacity(oldSchedule.getCurrentCapacity() + 1);
                scheduleRepository.save(oldSchedule);
            } else if (oldStatus == SocialServer.Status.INACTIVO && newStatus == SocialServer.Status.ACTIVO) {
                // Consume capacity
                if (oldSchedule.getCurrentCapacity() <= 0) {
                    throw new IllegalStateException(
                            "El horario no tiene capacidad disponible para reactivar al usuario.");
                }
                oldSchedule.setCurrentCapacity(oldSchedule.getCurrentCapacity() - 1);
                scheduleRepository.save(oldSchedule);
            }
        }

        // Manejar foto
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo);
            socialServer.setPhotoPath(photoPath);
        }

        // Actualizar EmergencyContact
        if (request.getTutorName() != null && request.getTutorPhone() != null) {
            if (socialServer.getEmergencyContact() != null) {
                socialServer.getEmergencyContact().setTutorName(request.getTutorName());
                socialServer.getEmergencyContact().setTutorPhone(request.getTutorPhone());
            } else {
                EmergencyContact emergencyContact = EmergencyContact.builder()
                        .tutorName(request.getTutorName())
                        .tutorPhone(request.getTutorPhone())
                        .build();
                socialServer.setEmergencyContact(emergencyContact);
            }
        }

        // Actualizar Period
        if (request.getPeriodId() != null) {
            Period period = periodRepository.findById(request.getPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró periodo con ID: " + request.getPeriodId()));
            socialServer.setPeriod(period);
        }

        // Actualizar el servidor social
        socialServer.setEmail(request.getEmail());
        socialServer.setName(request.getName());
        socialServer.setPark(park);
        socialServer.setSchool(request.getSchool());
        socialServer.setSchedule(newSchedule);
        socialServer.setTotalHoursRequired(request.getTotalHours());

        if (request.getEnrollmentDate() != null)
            socialServer.setEnrollmentDate(request.getEnrollmentDate());
        if (request.getStartDate() != null)
            socialServer.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)
            socialServer.setEndDate(request.getEndDate());

        socialServer.setStatus(newStatus);

        if (request.getBadge() != null)
            socialServer.setBadge(request.getBadge());
        if (request.getVest() != null)
            socialServer.setVest(request.getVest());
        if (request.getCellPhone() != null)
            socialServer.setCellPhone(request.getCellPhone());
        if (request.getBloodType() != null)
            socialServer.setBloodType(SocialServer.BloodType.valueOf(request.getBloodType()));
        if (request.getAllergy() != null)
            socialServer.setAllergy(request.getAllergy());
        if (request.getBirthDate() != null)
            socialServer.setBirthDate(request.getBirthDate());
        if (request.getMajor() != null)
            socialServer.setMajor(request.getMajor());
        if (request.getSocialServerType() != null)
            socialServer.setSocialServerType(SocialServer.SocialServerType.valueOf(request.getSocialServerType()));
        if (request.getGeneralInductionDate() != null)
            socialServer.setGeneralInductionDate(request.getGeneralInductionDate());
        if (request.getAcceptanceLetterId() != null)
            socialServer.setAcceptanceLetterId(request.getAcceptanceLetterId());
        if (request.getCompletionLetterId() != null)
            socialServer.setCompletionLetterId(request.getCompletionLetterId());

        SocialServer updatedSocialServer = socialServerRepository.save(socialServer);
        log.info("Servidor social actualizado exitosamente con ID: {}", updatedSocialServer.getId());

        return mapToResponse(updatedSocialServer, "Servidor social actualizado exitosamente");
    }

    public void deleteSocialServer(Long id) {
        log.info("Eliminando servidor social con ID: {}", id);

        SocialServer socialServer = socialServerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un servidor social con el ID: " + id));

        // Only release capacity if user was ACTIVO
        if (socialServer.getStatus() == SocialServer.Status.ACTIVO && socialServer.getSchedule() != null) {
            // Incrementar capacidad del horario
            com.ambu.asistencias.model.Schedule schedule = socialServer.getSchedule();
            schedule.setCurrentCapacity(schedule.getCurrentCapacity() + 1);
            scheduleRepository.save(schedule);
        }

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
        String days = null;
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (socialServer.getSchedule() != null) {
            scheduleId = socialServer.getSchedule().getId();
            days = socialServer.getSchedule().getDays();
            startTime = socialServer.getSchedule().getStartTime();
            endTime = socialServer.getSchedule().getEndTime();

            if (socialServer.getSchedule().getProgramPark() != null) {
                programId = socialServer.getSchedule().getProgramPark().getProgram().getId();
                programName = socialServer.getSchedule().getProgramPark().getProgram().getName();
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
                .days(days)
                .startTime(startTime)
                .endTime(endTime)
                .totalHoursRequired(socialServer.getTotalHoursRequired())
                .message(message)
                .enrollmentDate(socialServer.getEnrollmentDate())
                .startDate(socialServer.getStartDate())
                .endDate(socialServer.getEndDate())
                .status(socialServer.getStatus() != null ? socialServer.getStatus().name() : null)
                .photoPath(socialServer.getPhotoPath())
                .badge(socialServer.getBadge())
                .vest(socialServer.getVest())
                .tutorName(
                        socialServer.getEmergencyContact() != null ? socialServer.getEmergencyContact().getTutorName()
                                : null)
                .tutorPhone(
                        socialServer.getEmergencyContact() != null ? socialServer.getEmergencyContact().getTutorPhone()
                                : null)
                .cellPhone(socialServer.getCellPhone())
                .bloodType(socialServer.getBloodType() != null ? socialServer.getBloodType().name() : null)
                .allergy(socialServer.getAllergy())
                .birthDate(socialServer.getBirthDate())
                .major(socialServer.getMajor())
                .periodId(socialServer.getPeriod() != null ? socialServer.getPeriod().getId() : null)
                .periodStartDate(socialServer.getPeriod() != null ? socialServer.getPeriod().getStartDate() : null)
                .periodEndDate(socialServer.getPeriod() != null ? socialServer.getPeriod().getEndDate() : null)
                .socialServerType(
                        socialServer.getSocialServerType() != null ? socialServer.getSocialServerType().name() : null)
                .generalInductionDate(socialServer.getGeneralInductionDate())
                .acceptanceLetterId(socialServer.getAcceptanceLetterId())
                .completionLetterId(socialServer.getCompletionLetterId())
                .build();
    }
}
