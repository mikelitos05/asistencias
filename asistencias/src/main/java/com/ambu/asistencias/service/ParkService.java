package com.ambu.asistencias.service;

import com.ambu.asistencias.dto.ParkDTO;
import com.ambu.asistencias.dto.ParkRequest;
import com.ambu.asistencias.exception.ResourceAlreadyExistsException;
import com.ambu.asistencias.model.Park;
import com.ambu.asistencias.repository.ParkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParkService {

    private final ParkRepository parkRepository;

    public ParkDTO createPark(ParkRequest request) {
        log.info("Creando nuevo parque con nombre: {} y abreviatura: {}", 
                request.getParkName(), request.getAbbreviation());

        // Validar que el nombre del parque no exista
        if (parkRepository.findByParkNameIgnoreCase(request.getParkName()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Ya existe un parque con el nombre: " + request.getParkName());
        }

        // Validar que la abreviatura no exista
        if (parkRepository.findByAbbreviationIgnoreCase(request.getAbbreviation()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Ya existe un parque con la abreviatura: " + request.getAbbreviation());
        }

        // Crear el Park
        Park park = Park.builder()
                .parkName(request.getParkName())
                .abbreviation(request.getAbbreviation())
                .build();

        Park savedPark = parkRepository.save(park);
        log.info("Parque creado exitosamente con ID: {}", savedPark.getId());

        // Construir respuesta
        return ParkDTO.builder()
                .id(savedPark.getId())
                .parkName(savedPark.getParkName())
                .abbreviation(savedPark.getAbbreviation())
                .build();
    }
}

