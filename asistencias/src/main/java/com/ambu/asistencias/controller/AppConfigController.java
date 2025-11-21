package com.ambu.asistencias.controller;

import com.ambu.asistencias.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/configurations")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    @GetMapping("/photo-size-limit")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Integer>> getPhotoSizeLimit() {
        int limit = appConfigService.getMaxPhotoSizeMB();
        return ResponseEntity.ok(Map.of("value", limit));
    }

    @PutMapping("/photo-size-limit")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updatePhotoSizeLimit(@RequestBody Map<String, Integer> request) {
        Integer newValue = request.get("value");
        if (newValue == null || newValue <= 0) {
            return ResponseEntity.badRequest().build();
        }
        appConfigService.setMaxPhotoSizeMB(newValue);
        return ResponseEntity.ok().build();
    }
}
