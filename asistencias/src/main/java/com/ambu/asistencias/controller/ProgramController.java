package com.ambu.asistencias.controller;

import com.ambu.asistencias.dto.ProgramRequest;
import com.ambu.asistencias.dto.ProgramResponse;
import com.ambu.asistencias.dto.ScheduleRequest;
import com.ambu.asistencias.exception.ResourceNotFoundException;
import com.ambu.asistencias.model.Park;
import com.ambu.asistencias.model.Program;
import com.ambu.asistencias.model.ProgramPark;
import com.ambu.asistencias.model.Schedule;
import com.ambu.asistencias.repository.ParkRepository;
import com.ambu.asistencias.repository.ProgramParkRepository;
import com.ambu.asistencias.repository.ProgramRepository;
import com.ambu.asistencias.repository.ScheduleRepository;
import com.ambu.asistencias.repository.SocialServerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

        private final ProgramRepository programRepository;
        private final ScheduleRepository scheduleRepository;
        private final ParkRepository parkRepository;
        private final ProgramParkRepository programParkRepository;
        private final SocialServerRepository socialServerRepository;

        @GetMapping
        public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
                List<Program> programs = programRepository.findAll();
                List<ProgramResponse> responses = programs.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ProgramResponse> createProgram(@RequestBody @Valid ProgramRequest request) {
                // Create program
                Program program = Program.builder()
                                .name(request.getName())
                                .build();
                Program savedProgram = programRepository.save(program);

                // Create ProgramPark entries for each park
                for (Long parkId : request.getParkIds()) {
                        Park park = parkRepository.findById(parkId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Park not found with id " + parkId));

                        ProgramPark programPark = ProgramPark.builder()
                                        .program(savedProgram)
                                        .park(park)
                                        .build();
                        programParkRepository.save(programPark);
                }

                // Reload program with relationships
                Program updatedProgram = programRepository.findById(savedProgram.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

                return ResponseEntity.ok(mapToResponse(updatedProgram));
        }

        @PostMapping("/{programId}/schedules")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ProgramResponse> addSchedule(
                        @PathVariable Long programId,
                        @RequestBody @Valid ScheduleRequest request) {

                return programRepository.findById(programId).map(program -> {
                        // Find or validate the ProgramPark association
                        ProgramPark programPark = programParkRepository
                                        .findByProgramIdAndParkId(programId, request.getParkId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "El parque con ID " + request.getParkId()
                                                                        + " no está asociado a este programa"));

                        // Validate capacity
                        if (request.getCapacity() == null || request.getCapacity() <= 0) {
                                throw new IllegalArgumentException("La capacidad debe ser un número mayor a 0");
                        }

                        // Create schedule
                        Schedule schedule = Schedule.builder()
                                        .programPark(programPark)
                                        .days(request.getDays())
                                        .startTime(request.getStartTime())
                                        .endTime(request.getEndTime())
                                        .capacity(request.getCapacity())
                                        .currentCapacity(request.getCapacity())
                                        .build();

                        scheduleRepository.save(schedule);

                        // Refetch program to get updated state
                        Program updatedProgram = programRepository.findById(programId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

                        return ResponseEntity.ok(mapToResponse(updatedProgram));
                }).orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + programId));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ProgramResponse> updateProgram(@PathVariable Long id,
                        @RequestBody @Valid ProgramRequest request) {
                return programRepository.findById(id).map(program -> {
                        program.setName(request.getName());

                        // Update park associations
                        List<ProgramPark> currentProgramParks = programParkRepository.findByProgramId(id);
                        List<Long> currentParkIds = currentProgramParks.stream()
                                        .map(pp -> pp.getPark().getId())
                                        .collect(Collectors.toList());

                        // Remove parks that are no longer in the list
                        for (ProgramPark pp : currentProgramParks) {
                                if (!request.getParkIds().contains(pp.getPark().getId())) {
                                        // This will cascade delete schedules
                                        programParkRepository.delete(pp);
                                }
                        }

                        // Add new parks
                        for (Long parkId : request.getParkIds()) {
                                if (!currentParkIds.contains(parkId)) {
                                        Park park = parkRepository.findById(parkId)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Park not found with id " + parkId));

                                        ProgramPark programPark = ProgramPark.builder()
                                                        .program(program)
                                                        .park(park)
                                                        .build();
                                        programParkRepository.save(programPark);
                                }
                        }

                        programRepository.save(program);

                        // Reload program
                        Program updatedProgram = programRepository.findById(id)
                                        .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

                        return ResponseEntity.ok(mapToResponse(updatedProgram));
                }).orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + id));
        }

        @PutMapping("/{programId}/schedules/{scheduleId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ProgramResponse> updateSchedule(
                        @PathVariable Long programId,
                        @PathVariable Long scheduleId,
                        @RequestBody @Valid ScheduleRequest request) {

                return programRepository.findById(programId).map(program -> {
                        Schedule schedule = scheduleRepository.findById(scheduleId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Schedule not found with id " + scheduleId));

                        // Verify schedule belongs to this program
                        if (!schedule.getProgramPark().getProgram().getId().equals(programId)) {
                                throw new IllegalArgumentException("El horario no pertenece al programa especificado");
                        }

                        // If park changed, find/create new ProgramPark
                        if (!schedule.getProgramPark().getPark().getId().equals(request.getParkId())) {
                                ProgramPark newProgramPark = programParkRepository
                                                .findByProgramIdAndParkId(programId, request.getParkId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "El parque con ID " + request.getParkId()
                                                                                + " no está asociado a este programa"));
                                schedule.setProgramPark(newProgramPark);
                        }

                        // Validate capacity change
                        if (!schedule.getCapacity().equals(request.getCapacity())) {
                                int assignedUsers = schedule.getCapacity() - schedule.getCurrentCapacity();
                                if (request.getCapacity() < assignedUsers) {
                                        throw new IllegalArgumentException("La nueva capacidad ("
                                                        + request.getCapacity() +
                                                        ") no puede ser menor que la cantidad de usuarios asignados ("
                                                        + assignedUsers + ")");
                                }
                                schedule.setCurrentCapacity(request.getCapacity() - assignedUsers);
                                schedule.setCapacity(request.getCapacity());
                        }

                        schedule.setDays(request.getDays());
                        schedule.setStartTime(request.getStartTime());
                        schedule.setEndTime(request.getEndTime());

                        scheduleRepository.save(schedule);

                        // Refetch program to get updated state
                        Program updatedProgram = programRepository.findById(programId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

                        return ResponseEntity.ok(mapToResponse(updatedProgram));
                }).orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + programId));
        }

        private ProgramResponse mapToResponse(Program program) {
                List<ProgramResponse.ParkWithSchedules> parksList = new ArrayList<>();

                int totalCapacity = 0;
                int currentCapacity = 0;

                if (program.getProgramParks() != null) {
                        for (ProgramPark pp : program.getProgramParks()) {
                                List<ProgramResponse.ScheduleInfo> scheduleInfos = new ArrayList<>();

                                if (pp.getSchedules() != null) {
                                        for (Schedule schedule : pp.getSchedules()) {
                                                scheduleInfos.add(ProgramResponse.ScheduleInfo.builder()
                                                                .id(schedule.getId())
                                                                .days(schedule.getDays())
                                                                .startTime(schedule.getStartTime())
                                                                .endTime(schedule.getEndTime())
                                                                .capacity(schedule.getCapacity())
                                                                .currentCapacity(schedule.getCurrentCapacity())
                                                                .build());

                                                totalCapacity += schedule.getCapacity();
                                                currentCapacity += schedule.getCurrentCapacity();
                                        }
                                }

                                parksList.add(ProgramResponse.ParkWithSchedules.builder()
                                                .id(pp.getPark().getId())
                                                .parkName(pp.getPark().getParkName())
                                                .abbreviation(pp.getPark().getAbbreviation())
                                                .schedules(scheduleInfos)
                                                .build());
                        }
                }

                return ProgramResponse.builder()
                                .id(program.getId())
                                .name(program.getName())
                                .parks(parksList)
                                .totalCapacity(totalCapacity)
                                .currentCapacity(currentCapacity)
                                .build();
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<List<String>> deleteProgram(@PathVariable Long id) {
                Program program = programRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + id));

                // Find all ProgramParks and their schedules
                List<ProgramPark> programParks = programParkRepository.findByProgramId(id);
                List<String> affectedSocialServers = new ArrayList<>();

                for (ProgramPark pp : programParks) {
                        if (pp.getSchedules() != null) {
                                for (Schedule schedule : pp.getSchedules()) {
                                        // Find social servers linked to this schedule
                                        List<com.ambu.asistencias.model.SocialServer> socialServers = socialServerRepository
                                                        .findByScheduleId(schedule.getId());

                                        for (com.ambu.asistencias.model.SocialServer ss : socialServers) {
                                                ss.setSchedule(null);
                                                socialServerRepository.save(ss);
                                                affectedSocialServers.add(ss.getName());
                                        }
                                }
                        }
                }

                programRepository.delete(program);

                return ResponseEntity.ok(affectedSocialServers);
        }

        @DeleteMapping("/{programId}/schedules/{scheduleId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<List<String>> deleteSchedule(
                        @PathVariable Long programId,
                        @PathVariable Long scheduleId) {

                programRepository.findById(programId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Program not found with id " + programId));

                Schedule schedule = scheduleRepository.findById(scheduleId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Schedule not found with id " + scheduleId));

                if (!schedule.getProgramPark().getProgram().getId().equals(programId)) {
                        throw new IllegalArgumentException("El horario no pertenece al programa especificado");
                }

                // Find social servers linked to this schedule
                List<com.ambu.asistencias.model.SocialServer> socialServers = socialServerRepository
                                .findByScheduleId(scheduleId);

                List<String> affectedSocialServers = new ArrayList<>();

                for (com.ambu.asistencias.model.SocialServer ss : socialServers) {
                        ss.setSchedule(null);
                        socialServerRepository.save(ss);
                        affectedSocialServers.add(ss.getName());
                }

                scheduleRepository.delete(schedule);

                return ResponseEntity.ok(affectedSocialServers);
        }
}
