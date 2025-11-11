package com.ambu.asistencias.controller;

import com.ambu.asistencias.dto.UserRequest;
import com.ambu.asistencias.dto.UserResponse;
import com.ambu.asistencias.exception.ResourceNotFoundException;
import com.ambu.asistencias.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * AdminController - Endpoints administrativos
 * 
 * Este controlador maneja operaciones administrativas que no son CRUD estándar:
 * - Gestión de usuarios (solo SUPER_ADMIN)
 * - Acceso a fotos de asistencias
 * - Futuros: reportes, estadísticas, etc.
 */
@RestController
@RequestMapping("${api.prefix}/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final UserService userService;

    @Value("${app.upload.dir:uploads/photos}")
    private String uploadDir;

    /**
     * Obtiene una foto de asistencia
     * GET /api/admin/photos/{path}
     */
    @GetMapping("/photos/**")
    public ResponseEntity<Resource> getPhoto(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI();
            // Extract path after /photos/
            String photoPath = requestPath.substring(requestPath.indexOf("/photos/") + "/photos/".length());
            
            log.info("Solicitando foto. Request path: {}, Extracted path: {}", requestPath, photoPath);
            
            // The photoPath from database is stored as "uploads/photos/filename"
            // We need to resolve it from the project root
            Path basePath = Paths.get(System.getProperty("user.dir"));
            Path filePath = basePath.resolve(photoPath).normalize();
            
            // Security check: ensure the path is within the uploads directory
            Path uploadsPath = basePath.resolve("uploads/photos").normalize();
            if (!filePath.startsWith(uploadsPath)) {
                log.warn("Intento de acceso a ruta fuera del directorio permitido: {}", filePath);
                throw new ResourceNotFoundException("Ruta de foto no permitida");
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";
                String filename = resource.getFilename();
                if (filename != null) {
                    if (filename.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (filename.toLowerCase().endsWith(".gif")) {
                        contentType = "image/gif";
                    }
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                log.warn("Foto no encontrada o no accesible: {}", filePath);
                throw new ResourceNotFoundException("Foto no encontrada: " + photoPath);
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener la foto", e);
            throw new ResourceNotFoundException("Error al obtener la foto: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo usuario admin
     * POST /api/admin/users
     * Solo SUPER_ADMIN puede crear usuarios
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        log.info("Creando nuevo usuario admin: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los usuarios admin
     * GET /api/admin/users
     * Solo SUPER_ADMIN puede ver usuarios
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Obteniendo todos los usuarios admin");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}

