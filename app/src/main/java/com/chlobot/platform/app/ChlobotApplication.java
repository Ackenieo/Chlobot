package com.chlobot.platform.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.chlobot.platform")
public class ChlobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChlobotApplication.class, args);
    }
}
