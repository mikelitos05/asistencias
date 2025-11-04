package com.ambu.asistencias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialServerResponse {
    private Long id;
    private String email;
    private String name;
    private Long parkId;
    private String parkName;
    private String school;
    private String program;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalHoursRequired;
    private String message;
}
