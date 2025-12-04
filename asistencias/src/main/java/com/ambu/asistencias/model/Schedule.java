package com.ambu.asistencias.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "schedule_program_parks", joinColumns = @JoinColumn(name = "schedule_id"), inverseJoinColumns = @JoinColumn(name = "program_park_id"))
    @JsonIgnore
    private List<ProgramPark> programParks;

    @NotBlank
    @Column(nullable = false)
    private String days;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_capacity")
    private Integer currentCapacity;

    @Column(name = "career", length = 500)
    private String career;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
