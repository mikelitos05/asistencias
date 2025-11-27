package com.ambu.asistencias.controller;

import com.ambu.asistencias.model.Period;
import com.ambu.asistencias.service.PeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/periodos")
@RequiredArgsConstructor
public class PeriodController {

    private final PeriodService periodService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Period>> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Period> createPeriod(@RequestBody Period period) {
        return new ResponseEntity<>(periodService.createPeriod(period), HttpStatus.CREATED);
    }
}
