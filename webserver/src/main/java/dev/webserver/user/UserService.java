package dev.webserver.user;

import dev.webserver.external.mail.IMailService;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final IMailService mailService;

    public Optional<User> userByPrincipal(final String principal) {
        return repository.userByPrincipal(principal);
    }

    public Pageable<UserResponse> allUsers(final int page, final int size) {
        final Page of = Page.of(page, size);
        final Integer count = repository.countAllUsers();
        final var users = repository.listOfUsers(of)
                .stream()
                .map(s -> new UserResponse(s.firstname(), s.fullname(), s.email(), s.imageKey()))
                .toList();

        return new Pageable<>(of, count, users);
    }

    @Transactional(rollbackFor = Exception.class)
    public User create(final String fullname, final String firstname, final String email, final String imageKey) {
        final var optional = repository.userByPrincipal(email);
        if (optional.isPresent()) {
            final User user = optional.get();
            repository.updateUserImage(user.userId(), imageKey);
            user.setImageKey(imageKey);

            return user;
        }

        final User user = User.builder().firstname(firstname).fullname(fullname).email(email).imageKey(imageKey).build();
        final User saved = repository.save(user);
        mailService.registrationEmail(email, firstname);

        return saved;
    }
}
