package com.ambu.asistencias.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseSchemaFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Attempting to fix database schema for social_servers table...");
            // Modify schedule_id to be nullable
            jdbcTemplate.execute("ALTER TABLE social_servers MODIFY schedule_id BIGINT NULL");
            log.info("Successfully modified schedule_id column to be nullable.");
        } catch (Exception e) {
            log.error("Error fixing database schema: " + e.getMessage());
            // We don't rethrow because it might fail if the table doesn't exist or other
            // reasons,
            // and we don't want to stop the app startup if it's already fixed.
        }
    }
}
