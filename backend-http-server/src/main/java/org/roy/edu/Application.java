package org.roy.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: roy
 * @date: 2023/10/25 12:34
 * @description:
 */
@SpringBootApplication
@RestController
public class Application {
    @GetMapping("/http-demo/ping")
    public String ping(){
        return "pong";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
