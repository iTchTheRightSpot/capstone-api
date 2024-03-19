<<<<<<<< HEAD:webserver/src/test/java/dev/webserver/ApplicationTest.java
package dev.webserver;
========
package dev.capstone;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/test/java/dev/capstone/ApplicationTest.java

import org.springframework.boot.SpringApplication;

public class ApplicationTest {

    public static void main(String... args) {
        SpringApplication
                .from(Application::main)
                .with(TestConfig.class, TestController.class, DummyData.class)
                .run(args);
    }

}