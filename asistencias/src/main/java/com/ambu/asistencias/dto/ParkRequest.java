package com.ambu.asistencias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkRequest {
    
    @NotBlank(message = "El nombre del parque es obligatorio")
    @Size(max = 255, message = "El nombre del parque no puede exceder 255 caracteres")
    private String parkName;
    
    @NotBlank(message = "La abreviatura es obligatoria")
    @Size(max = 50, message = "La abreviatura no puede exceder 50 caracteres")
    private String abbreviation;
}

