package com.example.sarabrandserver.clientz.service;

import com.example.sarabrandserver.clientz.repository.ClientzRepository;
import com.example.sarabrandserver.enumeration.RoleEnum;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientzService {
    private final ClientzRepository clientzRepository;

    public ClientzService(ClientzRepository clientzRepository) {
        this.clientzRepository = clientzRepository;
    }

    /**
     *
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return List of ClientzPojo
     * */
    public List<?> fetchAllClientz(int page, int size) {
        return this.clientzRepository.fetchAll(RoleEnum.CLIENT, PageRequest.of(page, size));
    }

}
