package com.ambu.asistencias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    public enum AttendanceType {
        CHECK_IN,
        CHECK_OUT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_server_id", nullable = false)
    private SocialServer socialServer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceType type;

    @NotBlank
    @Size(max = 255)
    @Column(name = "photo_path", nullable = false, length = 255)
    private String photoPath;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

}