package com.ambu.asistencias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {
    private Long id;
    private String name;
    private ParkInfo park;
    private Integer totalCapacity;
    private Integer currentCapacity;
    private List<ScheduleInfo> schedules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParkInfo {
        private Long id;
        private String parkName;
        private String abbreviation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo {
        private Long id;
        private String days;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer capacity;
        private Integer currentCapacity;
    }
}
