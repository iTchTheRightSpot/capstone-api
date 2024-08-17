package dev.webserver.user;

import com.github.javafaker.Faker;
import dev.webserver.AbstractRepositoryTest;
import dev.webserver.util.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

final class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void userByPrincipal() {
        // pre-save
        final var user = repository.save(
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email(new Faker().internet().emailAddress())
                        .build());

        // method to test
        assertThat(repository.userByPrincipal(user.email()).isPresent()).isTrue();
    }

    @Test
    void listOfUsers() {
        // given
        repository.save(
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email(new Faker().internet().emailAddress())
                        .build());

        repository.save(
                User.builder()
                        .firstname(new Faker().name().firstName())
                        .fullname(new Faker().name().lastName())
                        .email("#" + new Faker().internet().emailAddress())
                        .build());

        // then
        assertThat(repository.listOfUsers(Page.of(0, 20)).isEmpty()).isFalse();
    }

}