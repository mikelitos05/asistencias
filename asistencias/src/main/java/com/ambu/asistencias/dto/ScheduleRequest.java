package com.ambu.asistencias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ScheduleRequest {

    @NotNull(message = "Debe seleccionar al menos un parque")
    private List<Long> parkIds; // Cambiado para soportar múltiples parques

    @NotBlank(message = "Los días son obligatorios")
    private String days;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @NotNull(message = "La capacidad es obligatoria")
    private Integer capacity;

    private String career; // Campo opcional para la carrera

    private String notes; // Campo opcional para notas adicionales
}
