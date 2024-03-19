<<<<<<<< HEAD:webserver/src/test/java/dev/webserver/AbstractUnitTest.java
package dev.webserver;
========
package dev.capstone;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/test/java/dev/capstone/AbstractUnitTest.java

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({ MockitoExtension.class, SpringExtension.class })
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class AbstractUnitTest{ }