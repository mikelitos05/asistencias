package com.ambu.asistencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ambu.asistencias.model.Attendance;
import com.ambu.asistencias.model.SocialServer;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>{
    List<Attendance> findBySocialServerOrderByTimestampDesc(SocialServer socialServer);
}
