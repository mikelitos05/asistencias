package com.ambu.asistencias.repository;

import com.ambu.asistencias.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ambu.asistencias.model.Program;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByProgram(Program program);
}
