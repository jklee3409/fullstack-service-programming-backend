package com.mycom.myapp.common.controller.health;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check API", description = "애플리케이션 상태 확인을 위한 Health Check API")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping()
    public String healthCheck() {
        return "Healthy!!!";
    }
}
