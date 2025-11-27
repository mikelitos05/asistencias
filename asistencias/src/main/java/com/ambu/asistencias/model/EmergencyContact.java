package com.ambu.asistencias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tutor_name", nullable = false)
    private String tutorName;

    @NotBlank
    @Column(name = "tutor_phone", nullable = false)
    private String tutorPhone;
}
