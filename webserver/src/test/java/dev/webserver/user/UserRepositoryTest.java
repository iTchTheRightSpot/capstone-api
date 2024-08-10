package dev.webserver.user;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.util.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email(principal)
                        .build()
        );

        repository.save(
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email("frank@fk.com")
                        .build());

        // method to test
        var optional = repository.userByPrincipal(principal);
        assertFalse(optional.isEmpty());
        assertEquals(user, optional.get());
    }

    @Test
    void listOfUsers() {
        // given
        repository.save(
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email("fk@fk.com")
                        .build());

        repository.save(
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email("frank@fk.com")
                        .build());

        // when
        var page = repository.listOfUsers(Page.of(0, 20));

        // then
        assertEquals(2, page.size());
    }

}