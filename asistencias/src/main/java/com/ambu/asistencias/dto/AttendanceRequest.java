package com.ambu.asistencias.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    @NotNull(message = "El folio es obligatorio")
    private Long id;

    @NotNull(message = "El ID del parque es obligatorio")
    private Long parkId;

    private String type; // "CHECK_IN" o "CHECK_OUT"

    private Double latitude;

    private Double longitude;

    private String address;
}
