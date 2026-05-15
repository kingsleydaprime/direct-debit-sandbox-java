package com.itc.direct_debit_sandbox.config;

import com.itc.direct_debit_sandbox.auth.AuthGuardInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthGuardInterceptor authGuardInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/documentation").setViewName("forward:/documentation.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authGuardInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Bootstrap endpoint — no prior provision needed
                        "/provision",
                        // Static home page assets
                        "/", "/index.html", "/docs.js", "/docs.css",
                        // Documentation page
                        "/documentation", "/documentation.html",
                        // Swagger UI (custom path + webjars)
                        "/docs", "/docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
                        // OpenAPI spec
                        "/v3/api-docs", "/v3/api-docs/**",
                        // Debug endpoints
                        "/debug/**"
                );
    }
}
