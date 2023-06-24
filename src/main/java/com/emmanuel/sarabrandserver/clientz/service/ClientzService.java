package com.emmanuel.sarabrandserver.clientz.service;

import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.emmanuel.sarabrandserver.enumeration.RoleEnum.CLIENT;

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
        return this.clientzRepository.fetchAll(CLIENT.toString(), PageRequest.of(page, size));
    }

}
