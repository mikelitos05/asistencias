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
            Schedule schedule = Schedule.builder()
                    .program(program)
                    .days(request.getDays())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .build();

            scheduleRepository.save(schedule);

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
