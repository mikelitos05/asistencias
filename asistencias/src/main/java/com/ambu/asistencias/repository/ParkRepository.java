package com.ambu.asistencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ambu.asistencias.model.Park;

import java.util.Optional;

@Repository
public interface ParkRepository extends JpaRepository<Park, Long>{
    Optional<Park> findByParkNameIgnoreCase(String parkName);
    Optional<Park> findByAbbreviationIgnoreCase(String abbreviation);
}
