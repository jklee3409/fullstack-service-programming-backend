package com.mycom.myapp.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbKeepAliveJob {
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void keepAlive() {
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("[DB-KEEP-ALIVE] SELECT 1 -> {}", one);
        } catch (Exception e) {
            log.warn("[DB-KEEP-ALIVE] failed: {}", e.getMessage());
        }
    }
}
