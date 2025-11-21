package com.ambu.asistencias.service;

import com.ambu.asistencias.model.AppConfig;
import com.ambu.asistencias.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;
    private static final String PHOTO_SIZE_LIMIT_KEY = "photo_size_limit_mb";
    private static final int DEFAULT_LIMIT_MB = 10;

    public int getMaxPhotoSizeMB() {
        return appConfigRepository.findByKey(PHOTO_SIZE_LIMIT_KEY)
                .map(config -> Integer.parseInt(config.getValue()))
                .orElse(DEFAULT_LIMIT_MB);
    }

    @Transactional
    public void setMaxPhotoSizeMB(int size) {
        AppConfig config = appConfigRepository.findByKey(PHOTO_SIZE_LIMIT_KEY)
                .orElse(AppConfig.builder().key(PHOTO_SIZE_LIMIT_KEY).build());

        config.setValue(String.valueOf(size));
        appConfigRepository.save(config);
    }
}
