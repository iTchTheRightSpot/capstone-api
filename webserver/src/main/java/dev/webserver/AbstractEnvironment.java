package dev.webserver;

import lombok.Setter;
import org.springframework.core.env.Environment;

import java.io.Serializable;

@Setter
public abstract class AbstractEnvironment {
    // paystack
    public record PaymentCredentialObj(String pubKey, String secretKey) implements Serializable {}

    protected String developerEmail;
    protected String developerFirstname;
    protected String developerLastName;
    private String paystackPubKey;
    private String paystackSecretKey;
    protected String ngnConversion;
    protected String usdConversion;
    protected Long raceConditionExpirationBound;
    protected String cartcookie;
    protected String cartCookieSplit;
    protected String jsessionid;
    protected Boolean cookiesecure;
    protected String cookiesamesite;
    protected String cookiepath;
    protected Integer cookiemaxage;
    protected String corsdomain;
    protected String baseurl;
    protected String activeprofile;
    protected String application;
    protected String uiRedirect;
    protected String mailExceptionRedirect;
    protected String awsbucket;
    protected Long shoppingSessionExpirationBoundInSeconds;
    protected String discord;
    protected String applicationContactEmail;
    protected String mailEncoding;

    protected AbstractEnvironment(final Environment environment) {
        // developer
        developerEmail = environment.getProperty("developer.email", "developer@email.com");
        developerFirstname = environment.getProperty("developer.firstname", "developer");
        developerLastName = environment.getProperty("developer.lastname", "developer");
        // payment
        paystackPubKey = environment.getProperty("paystack.pub.key", "paystack-pub-key");
        paystackSecretKey = environment.getProperty("paystack.secret.key", "paystack-secret-key");
        ngnConversion = environment.getProperty("capstone.ngn.to.kobo", "0.34");
        usdConversion = environment.getProperty("capstone.usd.to.cent", "100");
        raceConditionExpirationBound = environment.getProperty("race-condition.expiration.bound", Long.class, 900L);
        // cart
        cartcookie = environment.getProperty("cart.cookie.name", "CARTCOOKIE");
        cartCookieSplit = environment.getProperty("cart.split", "%");
        shoppingSessionExpirationBoundInSeconds = environment.getProperty("shopping.session.expiration.bound", Long.class, 1800L);
        // session cookie
        jsessionid = environment.getProperty("server.servlet.session.cookie.name", "JSESSIONID");
        cookiesecure = environment.getProperty("server.servlet.session.cookie.secure", Boolean.class, true);
        cookiesamesite = environment.getProperty("server.servlet.session.cookie.same-site", "lax");
        cookiepath = environment.getProperty("server.servlet.session.cookie.path", "/");
        cookiemaxage = environment.getProperty("server.servlet.session.cookie.max-age", Integer.class, 18000);
        // application
        application = environment.getProperty("spring.application.name", "Capstone Api");
        activeprofile = environment.getProperty("spring.profiles.active", "default");
        // custom
        corsdomain = environment.getProperty("cors.ui.domain", "http://localhost:4200/");
        baseurl = "/" + environment.getProperty("api.endpoint.baseurl", "api/v1/");
        applicationContactEmail = environment.getProperty("spring.mail.application.contact-email", "seu");
        uiRedirect = environment.getProperty("api.ui.redirect", "http://localhost:4200/");
        mailExceptionRedirect = baseurl + "mail/exception";
        // aws
        awsbucket = environment.getProperty("aws.bucket", "development");
        discord = environment.getProperty("application.log.webhook.discord", "discord");
        // mail
        mailEncoding = environment.getProperty("spring.mail.default-encoding", "UTF-8");
    }

    protected final PaymentCredentialObj payStackCredentials() {
        return new PaymentCredentialObj(paystackPubKey, paystackSecretKey);
    }

}
