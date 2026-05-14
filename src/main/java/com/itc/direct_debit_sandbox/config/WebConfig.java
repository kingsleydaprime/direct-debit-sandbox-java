package com.itc.direct_debit_sandbox.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Serve documentation.html when the browser navigates to /documentation
        registry.addViewController("/documentation").setViewName("forward:/documentation.html");
    }
}
