package org.hat.genaishield.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.hat.genaishield")
public class GenAIShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenAIShieldApplication.class, args);
    }
}
