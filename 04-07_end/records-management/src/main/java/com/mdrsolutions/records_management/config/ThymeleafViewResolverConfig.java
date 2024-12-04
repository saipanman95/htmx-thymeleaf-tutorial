package com.mdrsolutions.records_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class ThymeleafViewResolverConfig {

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @Bean(name = "viewResolver")
    public ThymeleafViewResolver viewResolver() {
        return thymeleafViewResolver;
    }
}