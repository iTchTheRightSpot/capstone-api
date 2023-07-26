package com.emmanuel.sarabrandserver.user.service;

import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.emmanuel.sarabrandserver.enumeration.RoleEnum.CLIENT;

@Service
public class ClientzService {
    private final UserRepository userRepository;

    public ClientzService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     *
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return List of ClientzPojo
     * */
    public List<?> fetchAllClientz(int page, int size) {
        return this.userRepository.fetchAll(CLIENT.toString(), PageRequest.of(page, size));
    }

}
