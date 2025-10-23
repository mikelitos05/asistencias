package com.ambu.asistencias.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ambu.asistencias.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
