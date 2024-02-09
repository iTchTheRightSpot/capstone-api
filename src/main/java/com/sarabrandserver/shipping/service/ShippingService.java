package com.sarabrandserver.shipping.service;

import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
import com.sarabrandserver.shipping.ShippingDto;
import com.sarabrandserver.shipping.ShippingMapper;
import com.sarabrandserver.shipping.entity.Shipping;
import com.sarabrandserver.shipping.repository.ShippingRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Defines the logic for countries we are allowed to ship to.
 * */
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepo repository;

    /**
     * Returns all {@code Shipping} from the db and
     * maps it to a {@code ShippingMapper}.
     * */
    public List<ShippingMapper> shipping() {
        return repository.findAll()
                .stream()
                .map(s -> new ShippingMapper(s.shippingId(), s.country(), s.ngnPrice(), s.usdPrice()))
                .toList();
    }

    /**
     * Saves a Shipping object to the db.
     *
     * @param dto is of {@code ShippingDto} which contains the
     *            necessary info to save a {@code Shipping} object.
     * @throws DuplicateException if dto.country() exists.
     * */
    @Transactional
    public void create(ShippingDto dto) {
        try {
        repository
                .save(new Shipping(StringUtils.capitalize(dto.country()), dto.ngn(), dto.usd()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("%s exists".formatted(dto.country()));
        }
    }

    /**
     * Updates a {@code Shipping} object.
     *
     * @param dto is of {@code ShippingMapper} which contains the
     *            necessary info to update a {@code Shipping} object.
     * @throws DuplicateException if dto.country() exists.
     * */
    @Transactional
    public void update(ShippingMapper dto) {
        try {
            repository
                    .updateShippingById(dto.id(), dto.country(), dto.ngn(), dto.usd());
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("%s exists".formatted(dto.country()));
        }
    }

    /**
     * Deletes a {@code Shipping} by its primary key.
     *
     * @param id is a primary key for a {@code Shipping} object.
     * @throws ResourceAttachedException if id is equal to 1.
     * */
    @Transactional
    public void delete(long id) {
        if (id == 1)
            throw new ResourceAttachedException("cannot delete default country.");

        repository.deleteShippingById(id);
    }

}
