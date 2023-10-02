package com.sarabrandserver.user.service;

import com.sarabrandserver.user.projection.ClientzPojo;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.sarabrandserver.enumeration.RoleEnum.CLIENT;

@Service
public class ClientzService {
    private final UserRepository userRepository;

    public ClientzService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Fetches all users with role client
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return Page of ClientzPojo
     * */
    public Page<ClientzPojo> allUsers(int page, int size) {
        return this.userRepository.fetchAll(CLIENT.toString(), PageRequest.of(page, size));
    }

}
