package com.ambu.asistencias.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ambu.asistencias.dto.ParkDTO;
import com.ambu.asistencias.dto.ParkRequest;
import com.ambu.asistencias.model.Park;
import com.ambu.asistencias.repository.ParkRepository;
import com.ambu.asistencias.service.ParkService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/parques")
@RequiredArgsConstructor
@Slf4j
public class ParkController {

    private final ParkRepository parkRepository;
    private final ParkService parkService;

    @GetMapping
    public ResponseEntity<List<ParkDTO>> getAllParks() {
        List<Park> parks = parkRepository.findAll();
        List<ParkDTO> parksDTO = parks.stream()
                .map(park -> ParkDTO.builder()
                        .id(park.getId())
                        .parkName(park.getParkName())
                        .abbreviation(park.getAbbreviation())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(parksDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkDTO> getParkById(@PathVariable Long id) {
        ParkDTO park = parkService.getParkById(id);
        return ResponseEntity.ok(park);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParkDTO> createPark(
            @Valid @RequestBody ParkRequest request) {
        
        log.info("Solicitud de creación de parque recibida");
        ParkDTO response = parkService.createPark(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParkDTO> updatePark(
            @PathVariable Long id,
            @Valid @RequestBody ParkRequest request) {
        
        log.info("Solicitud de actualización de parque con ID: {}", id);
        ParkDTO response = parkService.updatePark(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deletePark(@PathVariable Long id) {
        log.info("Solicitud de eliminación de parque con ID: {}", id);
        parkService.deletePark(id);
        return ResponseEntity.noContent().build();
    }
}
