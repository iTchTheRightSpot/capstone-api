package dev.webserver;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({ MockitoExtension.class, SpringExtension.class })
@TestPropertySource(locations = "classpath:application.yml")
public abstract class AbstractUnitTest {

    protected <T extends AbstractEnvironment> void setUpEnvironmentVariables(final T type) {
        type.setDeveloperEmail("developer@email.com");
        type.setDeveloperFirstname("developer");
        type.setDeveloperLastName("developer");

        type.setPaystackPubKey("paystack-pub-key");
        type.setPaystackSecretKey("paystack-pub-key");
        type.setNgnConversion("0.34");
        type.setUsdConversion("100");
        type.setRaceConditionExpirationBound(900L);

        type.setCartcookie("CARTCOOKIE");
        type.setCartCookieSplit("%");

        type.setJsessionid("JSESSIONID");
        type.setCookiesecure(false);
        type.setCookiesamesite("lax");
        type.setCookiemaxage(18000);

        type.setActiveprofile("test");
        type.setApplication("Capstone Api");

        type.setCorsdomain("http://localhost:4200/");
        type.setUiRedirect("http://localhost:4200/");
        type.setBaseurl("api/v1/");
        type.setAwsbucket("development");
        type.setShoppingSessionExpirationBoundInSeconds(1800L);
        type.setDiscord("discord");
        type.setApplicationContactEmail("developer@email.com");
    }

}