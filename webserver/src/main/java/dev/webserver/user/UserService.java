package dev.webserver.user;

import dev.webserver.enumeration.RoleEnum;
import dev.webserver.external.mail.IMailService;
import dev.webserver.security.UserDetailz;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
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
    public UserDetailz create(final String fullname, final String firstname, final String email, final String imageKey) {
        final var optional = repository.userByPrincipal(email);

        if (optional.isPresent()) {
            final User user = optional.get();
            repository.updateUserImage(user.userId(), imageKey);
            user.setImageKey(imageKey);
            return new UserDetailz(user, roleRepository.allRolesByUserId(user.userId()));
        }

        final User user = repository.save(User.builder().firstname(firstname).fullname(fullname).email(email).imageKey(imageKey).build());
        mailService.registrationEmail(email, firstname);

        return new UserDetailz(user, List.of(roleRepository.save(new Role(null, RoleEnum.USER, user.userId()))));
    }
}
