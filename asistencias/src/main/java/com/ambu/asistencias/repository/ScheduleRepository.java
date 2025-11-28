package com.ambu.asistencias.repository;

import com.ambu.asistencias.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // Schedules are now accessed through ProgramPark.getSchedules()
    // No need for findByProgram since Schedule.program no longer exists
}
