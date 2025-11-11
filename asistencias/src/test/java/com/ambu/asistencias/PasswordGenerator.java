package com.ambu.asistencias;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "prueba123456"; // Tu contraseña
        String encodedPassword = encoder.encode(password);
        System.out.println("Contraseña encriptada: " + encodedPassword);
    }
}
