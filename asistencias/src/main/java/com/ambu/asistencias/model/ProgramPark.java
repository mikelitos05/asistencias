package com.ambu.asistencias.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "program_parks", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "program_id", "park_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramPark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    @JsonIgnore
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    @JsonIgnore
    private Park park;

    @ManyToMany(mappedBy = "programParks")
    private List<Schedule> schedules;
}
