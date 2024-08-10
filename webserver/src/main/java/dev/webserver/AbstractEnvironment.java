package dev.webserver;

import org.springframework.core.env.Environment;

import java.io.Serializable;

public abstract class AbstractEnvironment {
    // paystack
    public record PaymentCredentialObj(String pubKey, String secretKey) implements Serializable {}

    protected final String developerEmail;
    protected final String developerFirstname;
    protected final String developerLastName;
    protected final String paystackPubKey;
    protected final String paystackSecretKey;
    protected final String ngnConversion;
    protected final String usdConversion;
    protected final long raceConditionExpirationBound;
    protected final String cartcookie;
    protected final String cartCookieSplit;
    protected final String jsessionid;
    protected final boolean cookiesecure;
    protected final String samesite;
    protected final String cookiepath;
    protected final int maxage;
    protected final String corsdomain;
    protected final String baseurl;
    protected final String activeprofile;
    protected final String application;
    protected final String uiRedirect;
    protected final String mailExceptionRedirect;
    protected final String awsbucket;
    protected final long shoppingSessionBoundInSeconds;
    protected final String discord;
    protected final String applicationContactEmail;

    protected AbstractEnvironment(Environment environment) {
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
        shoppingSessionBoundInSeconds = environment.getProperty("shopping.session.expiration.bound", Long.class, 18000L);
        // session cookie
        jsessionid = environment.getProperty("server.servlet.session.cookie.name", "JSESSIONID");
        cookiesecure = environment.getProperty("server.servlet.session.cookie.secure", Boolean.class, true);
        samesite = environment.getProperty("server.servlet.session.cookie.same-site", "lax");
        cookiepath = environment.getProperty("server.servlet.session.cookie.path", "/");
        maxage = environment.getProperty("server.servlet.session.cookie.max-age", Integer.class, 18000);
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
    }

    protected final PaymentCredentialObj payStackCredentials() {
        return new PaymentCredentialObj(paystackPubKey, paystackSecretKey);
    }

}
