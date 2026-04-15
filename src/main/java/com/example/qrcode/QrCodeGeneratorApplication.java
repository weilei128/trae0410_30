package com.example.qrcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QrCodeGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(QrCodeGeneratorApplication.class, args);
    }
}
