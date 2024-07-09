package dev.webserver.user;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void userByPrincipal() {
        // given
        String principal = "hello@hello.com";

        // method to test
        assertTrue(repository.userByPrincipal(principal).isEmpty());

        // pre-save
        var user = repository.save(
                SarreBrandUser.builder()
                        .firstname(new Faker().name().firstName())
                        .lastname(new Faker().name().lastName())
                        .email(principal)
                        .phoneNumber("0000000000")
                        .password("password")
                        .enabled(true)
                        .clientRole(new HashSet<>())
                        .paymentDetail(new HashSet<>())
                        .build()
        );

        repository.save(
                SarreBrandUser.builder()
                        .firstname(new Faker().name().firstName())
                        .lastname(new Faker().name().lastName())
                        .email("frank@fk.com")
                        .phoneNumber("0000000000")
                        .password("password")
                        .enabled(true)
                        .clientRole(new HashSet<>())
                        .paymentDetail(new HashSet<>())
                        .build()
        );

        // method to test
        var optional = repository.userByPrincipal(principal);
        assertFalse(optional.isEmpty());
        assertEquals(user, optional.get());
    }

    @Test
    void allUsers() {
        // given
        repository.save(
                SarreBrandUser.builder()
                        .firstname(new Faker().name().firstName())
                        .lastname(new Faker().name().lastName())
                        .email("fk@fk.com")
                        .phoneNumber("0000000000")
                        .password("password")
                        .enabled(true)
                        .clientRole(new HashSet<>())
                        .paymentDetail(new HashSet<>())
                        .build()
        );

        repository.save(
                SarreBrandUser.builder()
                        .firstname(new Faker().name().firstName())
                        .lastname(new Faker().name().lastName())
                        .email("frank@fk.com")
                        .phoneNumber("0000000000")
                        .password("password")
                        .enabled(true)
                        .clientRole(new HashSet<>())
                        .paymentDetail(new HashSet<>())
                        .build()
        );

        // when
        var page = repository.allUsers(PageRequest.of(0, 20));

        // then
        assertEquals(2, page.getNumberOfElements());
    }

}