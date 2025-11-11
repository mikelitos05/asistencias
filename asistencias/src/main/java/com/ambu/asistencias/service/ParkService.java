package com.ambu.asistencias.service;

import com.ambu.asistencias.dto.ParkDTO;
import com.ambu.asistencias.dto.ParkRequest;
import com.ambu.asistencias.exception.ResourceAlreadyExistsException;
import com.ambu.asistencias.exception.ResourceNotFoundException;
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

    public ParkDTO getParkById(Long id) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + id));
        
        return ParkDTO.builder()
                .id(park.getId())
                .parkName(park.getParkName())
                .abbreviation(park.getAbbreviation())
                .build();
    }

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

    public ParkDTO updatePark(Long id, ParkRequest request) {
        log.info("Actualizando parque con ID: {}", id);

        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + id));

        // Validar que el nombre del parque no exista en otro parque
        parkRepository.findByParkNameIgnoreCase(request.getParkName())
                .ifPresent(existingPark -> {
                    if (!existingPark.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException(
                                "Ya existe un parque con el nombre: " + request.getParkName());
                    }
                });

        // Validar que la abreviatura no exista en otro parque
        parkRepository.findByAbbreviationIgnoreCase(request.getAbbreviation())
                .ifPresent(existingPark -> {
                    if (!existingPark.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException(
                                "Ya existe un parque con la abreviatura: " + request.getAbbreviation());
                    }
                });

        // Actualizar el parque
        park.setParkName(request.getParkName());
        park.setAbbreviation(request.getAbbreviation());

        Park updatedPark = parkRepository.save(park);
        log.info("Parque actualizado exitosamente con ID: {}", updatedPark.getId());

        return ParkDTO.builder()
                .id(updatedPark.getId())
                .parkName(updatedPark.getParkName())
                .abbreviation(updatedPark.getAbbreviation())
                .build();
    }

    public void deletePark(Long id) {
        log.info("Eliminando parque con ID: {}", id);

        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un parque con el ID: " + id));

        //TODO: Preguntar si se puede eliminar el parque si tiene servidores sociales asociados
        // Verificar si el parque tiene servidores sociales asociados
        if (park.getSocialServers() != null && !park.getSocialServers().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el parque porque tiene servidores sociales asociados");
        }

        parkRepository.delete(park);
        log.info("Parque eliminado exitosamente con ID: {}", id);
    }
}

