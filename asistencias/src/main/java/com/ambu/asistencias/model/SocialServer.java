package com.ambu.asistencias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "social_servers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String school;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String program;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Column(name = "total_hours_required", nullable = false)
    private Integer totalHoursRequired;

    @OneToMany(mappedBy = "socialServer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;
}