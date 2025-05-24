package com.example.qrcodegenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import java.time.Clock;

@Slf4j
@EnableCaching
@SpringBootApplication
public class QRCodeGeneratorApplication
{
    public static void main(String[] args)
    {
        try
        {
            log.info("Starting QRCodeGenerator application...");
            SpringApplication.run(QRCodeGeneratorApplication.class, args);
            log.info("Application started successfully");
        }
        catch (Exception e)
        {
            log.error("Application startup failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    @Bean
    public Clock clock()
    {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ObjectMapper objectMapper()
    {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}