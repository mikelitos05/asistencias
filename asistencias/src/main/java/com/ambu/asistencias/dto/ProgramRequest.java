package com.ambu.asistencias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRequest {

    @NotBlank(message = "El nombre del programa es obligatorio")
    private String name;

    @NotEmpty(message = "Debe seleccionar al menos un parque")
    private List<Long> parkIds;
}
