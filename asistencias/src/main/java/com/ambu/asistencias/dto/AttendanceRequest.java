package com.ambu.asistencias.dto;

import jakarta.validation.constraints.Email;
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
    
    @Email(message = "El correo electrónico debe tener un formato válido")
    @NotNull(message = "El correo electrónico es obligatorio")
    private String email;
    
    @NotNull(message = "El ID del parque es obligatorio")
    private Long parkId;
    
    // Opcional: tipo de asistencia, si no se envía se determina automáticamente
    private String type; // "CHECK_IN" o "CHECK_OUT"
}
