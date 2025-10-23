package com.ambu.asistencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ambu.asistencias.model.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{

}
