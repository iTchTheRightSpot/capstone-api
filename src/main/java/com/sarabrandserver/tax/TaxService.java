package com.sarabrandserver.tax;

import com.sarabrandserver.exception.CustomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxRepository repository;

    public List<TaxDto> taxes() {
        return repository
                .findAll()
                .stream()
                .map(t -> new TaxDto(t.taxId(), t.name(), t.percentage()))
                .toList();
    }

    public void update(TaxDto dto) {
        repository
                .updateByTaxId(dto.id(), dto.name(), dto.percentage());
    }

    public Tax taxById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("cannot find tax information"));
    }

}