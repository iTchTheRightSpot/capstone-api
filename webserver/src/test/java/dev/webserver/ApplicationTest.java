package dev.webserver;

import org.springframework.boot.SpringApplication;

class ApplicationTest {

    public static void main(String... args) {
        SpringApplication
                .from(Application::main)
                .with(TestConfig.class, TestController.class, DummyData.class)
                .run(args);
    }

}