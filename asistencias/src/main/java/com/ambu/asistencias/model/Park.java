package com.ambu.asistencias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "parks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Park {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "park_name", nullable = false, unique = true, length = 255)
    private String parkName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "acronym", nullable = false, unique = true, length = 50)
    private String acronym;

    @OneToMany(mappedBy = "park", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<User> users;
}