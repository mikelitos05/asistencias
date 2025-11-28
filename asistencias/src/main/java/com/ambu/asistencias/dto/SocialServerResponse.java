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
    private Long programId;
    private String program;
    private Long scheduleId;
    private String days;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalHoursRequired;
    private String message;

    private java.time.LocalDate enrollmentDate;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private String status;
    private String photoPath;
    private Boolean badge;
    private Integer vest;
    private String tutorName;
    private String tutorPhone;
    private String cellPhone;
    private String bloodType;
    private String allergy;
    private java.time.LocalDate birthDate;
    private String major;
    private Long periodId;
    private java.time.LocalDate periodStartDate;
    private java.time.LocalDate periodEndDate;
    private String socialServerType;
    private java.time.LocalDate generalInductionDate;
    private String acceptanceLetterId;
    private String completionLetterId;
}
