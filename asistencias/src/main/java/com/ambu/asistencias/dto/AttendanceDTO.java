package com.ambu.asistencias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Long socialServerId;
    private String socialServerName;
    private String socialServerEmail;
    private Long parkId;
    private String parkName;
    private LocalDateTime timestamp;
    private String type;
    private String photoPath;
}

