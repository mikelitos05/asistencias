package com.ambu.asistencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ambu.asistencias.model.SocialServer;

import java.util.Optional;

@Repository
public interface SocialServerRepository extends JpaRepository<SocialServer, Long>{
    Optional<SocialServer> findByEmail(String email);
}
