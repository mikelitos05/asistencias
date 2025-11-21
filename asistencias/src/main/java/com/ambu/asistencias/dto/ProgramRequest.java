package com.ambu.asistencias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRequest {

    @NotBlank(message = "El nombre del programa es obligatorio")
    private String name;

    @NotNull(message = "El ID del parque es obligatorio")
    private Long parkId;

    @NotNull(message = "La capacidad total es obligatoria")
    private Integer totalCapacity;
}
