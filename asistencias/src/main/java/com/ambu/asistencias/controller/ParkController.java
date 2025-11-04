package com.ambu.asistencias.controller;

import com.ambu.asistencias.dto.ParkDTO;
import com.ambu.asistencias.dto.ParkRequest;
import com.ambu.asistencias.model.Park;
import com.ambu.asistencias.repository.ParkRepository;
import com.ambu.asistencias.service.ParkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parques")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
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

    @PostMapping
    public ResponseEntity<ParkDTO> createPark(
            @Valid @RequestBody ParkRequest request) {
        
        log.info("Solicitud de creación de parque recibida");
        ParkDTO response = parkService.createPark(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
