package com.example.helpdesk.config;

import com.example.helpdesk.persistence.TicketEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.example.helpdesk")
@EntityScan(basePackageClasses = TicketEntity.class)
@EnableConfigurationProperties(DownstreamProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}