package com.ambu.asistencias.repository;

import com.ambu.asistencias.model.ProgramPark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramParkRepository extends JpaRepository<ProgramPark, Long> {

    Optional<ProgramPark> findByProgramIdAndParkId(Long programId, Long parkId);

    List<ProgramPark> findByProgramId(Long programId);

    List<ProgramPark> findByParkId(Long parkId);

    boolean existsByProgramIdAndParkId(Long programId, Long parkId);
}
