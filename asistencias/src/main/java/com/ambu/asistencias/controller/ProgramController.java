package com.ambu.asistencias.controller;

import com.ambu.asistencias.dto.ProgramRequest;
import com.ambu.asistencias.dto.ProgramResponse;
import com.ambu.asistencias.dto.ScheduleRequest;
import com.ambu.asistencias.exception.ResourceNotFoundException;
import com.ambu.asistencias.model.Program;
import com.ambu.asistencias.model.Schedule;
import com.ambu.asistencias.repository.ParkRepository;
import com.ambu.asistencias.repository.ProgramRepository;
import com.ambu.asistencias.repository.ScheduleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

        private final ProgramRepository programRepository;
        private final ScheduleRepository scheduleRepository;
        private final ParkRepository parkRepository;

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
                return parkRepository.findById(request.getParkId()).map(park -> {
                        Program program = Program.builder()
                                        .name(request.getName())
                                        .park(park)
                                        .totalCapacity(request.getTotalCapacity())
                                        .currentCapacity(request.getTotalCapacity())
                                        .build();
                        Program savedProgram = programRepository.save(program);
                        return ResponseEntity.ok(mapToResponse(savedProgram));
                }).orElseThrow(() -> new ResourceNotFoundException("Park not found with id " + request.getParkId()));
        }

        @PostMapping("/{programId}/schedules")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ProgramResponse> addSchedule(
                        @PathVariable Long programId,
                        @RequestBody @Valid ScheduleRequest request) {

                return programRepository.findById(programId).map(program -> {
                        // Validar que la capacidad sea obligatoria
                        if (request.getCapacity() == null) {
                                throw new IllegalArgumentException("La capacidad es obligatoria");
                        }

                        // Validar capacidad acumulada
                        int currentTotalScheduleCapacity = program.getSchedules() != null
                                        ? program.getSchedules().stream().mapToInt(Schedule::getCapacity).sum()
                                        : 0;

                        if (currentTotalScheduleCapacity + request.getCapacity() > program.getTotalCapacity()) {
                                throw new IllegalArgumentException("La suma de las capacidades de los horarios (" +
                                                (currentTotalScheduleCapacity + request.getCapacity()) +
                                                ") excede la capacidad total del programa ("
                                                + program.getTotalCapacity() + ")");
                        }

                        Schedule schedule = Schedule.builder()
                                        .program(program)
                                        .days(request.getDays())
                                        .startTime(request.getStartTime())
                                        .endTime(request.getEndTime())
                                        .capacity(request.getCapacity())
                                        .currentCapacity(request.getCapacity())
                                        .build();

                        scheduleRepository.save(schedule);

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
                        // Validar cambio de capacidad
                        if (!program.getTotalCapacity().equals(request.getTotalCapacity())) {
                                int assignedUsers = program.getTotalCapacity() - program.getCurrentCapacity();
                                if (request.getTotalCapacity() < assignedUsers) {
                                        throw new IllegalArgumentException("La nueva capacidad total ("
                                                        + request.getTotalCapacity() +
                                                        ") no puede ser menor que la cantidad de usuarios asignados ("
                                                        + assignedUsers + ")");
                                }
                                program.setCurrentCapacity(request.getTotalCapacity() - assignedUsers);
                                program.setTotalCapacity(request.getTotalCapacity());
                        }

                        program.setName(request.getName());

                        if (!program.getPark().getId().equals(request.getParkId())) {
                                parkRepository.findById(request.getParkId()).ifPresent(program::setPark);
                        }

                        Program updatedProgram = programRepository.save(program);
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

                        if (!schedule.getProgram().getId().equals(programId)) {
                                throw new IllegalArgumentException("El horario no pertenece al programa especificado");
                        }

                        // Validar cambio de capacidad
                        if (!schedule.getCapacity().equals(request.getCapacity())) {
                                int assignedUsers = schedule.getCapacity() - schedule.getCurrentCapacity();
                                if (request.getCapacity() < assignedUsers) {
                                        throw new IllegalArgumentException("La nueva capacidad ("
                                                        + request.getCapacity() +
                                                        ") no puede ser menor que la cantidad de usuarios asignados ("
                                                        + assignedUsers + ")");
                                }

                                // Validar capacidad acumulada en el programa
                                int otherSchedulesCapacity = program.getSchedules().stream()
                                                .filter(s -> !s.getId().equals(scheduleId))
                                                .mapToInt(Schedule::getCapacity)
                                                .sum();

                                if (otherSchedulesCapacity + request.getCapacity() > program.getTotalCapacity()) {
                                        throw new IllegalArgumentException(
                                                        "La suma de las capacidades de los horarios excede la capacidad total del programa");
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
                List<ProgramResponse.ScheduleInfo> scheduleInfos = program.getSchedules() != null
                                ? program.getSchedules().stream()
                                                .map(schedule -> ProgramResponse.ScheduleInfo.builder()
                                                                .id(schedule.getId())
                                                                .days(schedule.getDays())
                                                                .startTime(schedule.getStartTime())
                                                                .endTime(schedule.getEndTime())
                                                                .capacity(schedule.getCapacity())
                                                                .currentCapacity(schedule.getCurrentCapacity())
                                                                .build())
                                                .collect(Collectors.toList())
                                : List.of();

                return ProgramResponse.builder()
                                .id(program.getId())
                                .name(program.getName())
                                .park(ProgramResponse.ParkInfo.builder()
                                                .id(program.getPark().getId())
                                                .parkName(program.getPark().getParkName())
                                                .abbreviation(program.getPark().getAbbreviation())
                                                .build())
                                .totalCapacity(program.getTotalCapacity())
                                .currentCapacity(program.getCurrentCapacity())
                                .schedules(scheduleInfos)
                                .build();
        }
}
