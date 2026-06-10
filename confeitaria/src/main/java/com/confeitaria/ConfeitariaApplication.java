package com.confeitaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ponto de entrada da aplicação. O Spring Boot escaneia automaticamente todos os
// @Controller, @Service, @Repository e @Configuration nos subpacotes daqui.
@SpringBootApplication
public class ConfeitariaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfeitariaApplication.class, args);
    }
}
