package com.ambu.asistencias.service;

import com.ambu.asistencias.model.Period;
import com.ambu.asistencias.repository.PeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodService {

    private final PeriodRepository periodRepository;

    public List<Period> getAllPeriods() {
        return periodRepository.findAll();
    }

    @Transactional
    public Period createPeriod(Period period) {
        return periodRepository.save(period);
    }
}
