package com.ambu.asistencias.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfig {

    @Id
    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @Column(name = "config_value", nullable = false)
    private String value;
}
