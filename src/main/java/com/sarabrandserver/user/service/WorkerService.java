package com.sarabrandserver.user.service;

import com.sarabrandserver.user.repository.UserRepository;
import com.sarabrandserver.user.res.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final UserRepository userRepository;

    /**
     * Returns a page of users
     *
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return Page of ClientzPojo
     * */
    public Page<UserResponse> allUsers(int page, int size) {
        return this.userRepository
                .allUsers(PageRequest.of(page, size))
                .map(s -> new UserResponse(s.getFirstname(), s.getLastname(), s.getEmail(), s.getPhoneNumber()));
    }

}
