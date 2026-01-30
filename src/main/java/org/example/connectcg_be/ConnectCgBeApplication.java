package org.example.connectcg_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConnectCgBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectCgBeApplication.class, args);
    }

}
