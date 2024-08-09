package dev.webserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SarreBrandUserService {

    private final UserRepository userRepository;


    public Optional<SarreBrandUser> userByPrincipal(String principal) {
        return userRepository.userByPrincipal(principal);
    }

    /**
     * Returns a {@link Page} of {@link SarreBrandUser}.
     *
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return {@link Page} of {@link UserResponse}.
     * */
    public Page<UserResponse> allUsers(int page, int size) {
        return userRepository
                .allUsers()
                .map(s -> new UserResponse(s.firstname(), s.lastname(), s.email(), s.phoneNumber()));
    }

}
