package com.ambu.asistencias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkDTO {
    private Long id;
    private String parkName;
    private String abbreviation;
}

