package com.ambu.asistencias.repository;

import com.ambu.asistencias.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
    Optional<AppConfig> findByKey(String key);
}
