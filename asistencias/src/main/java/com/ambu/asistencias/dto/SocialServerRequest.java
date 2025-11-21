package com.ambu.asistencias.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialServerRequest {

    @Email(message = "El correo electrónico debe tener un formato válido")
    @NotBlank(message = "El correo electrónico es obligatorio")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String name;

    @NotNull(message = "El ID del parque es obligatorio")
    private Long parkId;

    @NotBlank(message = "La escuela es obligatoria")
    @Size(max = 100, message = "La escuela no puede exceder 100 caracteres")
    private String school;

    @NotNull(message = "El ID del horario es obligatorio")
    private Long scheduleId;

    @NotNull(message = "Las horas totales requeridas son obligatorias")
    @Min(value = 1, message = "Las horas totales deben ser al menos 1")
    @Max(value = 10000, message = "Las horas totales no pueden exceder 10000")
    private Integer totalHours;
}
