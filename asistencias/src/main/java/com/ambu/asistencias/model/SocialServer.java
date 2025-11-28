package com.ambu.asistencias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = true)
    private Schedule schedule;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String school;

    @NotNull
    @Column(name = "total_hours_required", nullable = false)
    private Integer totalHoursRequired;

    @OneToMany(mappedBy = "socialServer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;

    @Column(name = "enrollment_date", nullable = false)
    private java.time.LocalDate enrollmentDate;

    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(nullable = false)
    private Boolean badge;

    @Column(nullable = false)
    private Integer vest;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "emergency_contact_id", referencedColumnName = "id")
    private EmergencyContact emergencyContact;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type")
    private BloodType bloodType;

    @Column(name = "allergy")
    private String allergy;

    @Column(name = "birth_date", nullable = false)
    private java.time.LocalDate birthDate;

    @Column(name = "major")
    private String major;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "period_id")
    private Period period;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_server_type", nullable = false)
    private SocialServerType socialServerType;

    @Column(name = "general_induction_date")
    private java.time.LocalDate generalInductionDate;

    @Column(name = "acceptance_letter_id")
    private String acceptanceLetterId;

    @Column(name = "completion_letter_id")
    private String completionLetterId;

    public enum Status {
        ACTIVO, INACTIVO
    }

    public enum BloodType {
        A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE, DESCONOCE
    }

    public enum SocialServerType {
        PRACTICANTE_SOCIAL, SERVIDOR_SOCIAL
    }
}