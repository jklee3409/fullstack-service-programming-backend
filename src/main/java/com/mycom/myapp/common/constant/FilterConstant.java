package com.mycom.myapp.common.constant;

public class FilterConstant {

    public static final String[] WHITE_LIST = {
            // health check
            "/api/health",

            // auth
            "/api/auth/**",

            // swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}
