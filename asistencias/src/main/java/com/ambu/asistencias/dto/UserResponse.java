package com.ambu.asistencias.dto;

import com.ambu.asistencias.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private User.Role role;
    private LocalDateTime registrationDate;
    private Boolean active;
}

