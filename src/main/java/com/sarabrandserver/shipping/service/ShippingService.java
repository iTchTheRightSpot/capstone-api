package com.sarabrandserver.shipping.service;

import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
import com.sarabrandserver.shipping.ShippingDto;
import com.sarabrandserver.shipping.ShippingMapper;
import com.sarabrandserver.shipping.entity.ShipSetting;
import com.sarabrandserver.shipping.repository.ShippingRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    private final ShippingRepo repository;

    /**
     * Returns all {@code ShipSetting} from the db and
     * maps it to a {@code ShippingMapper}.
     * */
    public List<ShippingMapper> shipping() {
        return repository.findAll()
                .stream()
                .map(s -> new ShippingMapper(s.shipId(), s.country(), s.ngnPrice(), s.usdPrice()))
                .toList();
    }

    /**
     * Saves a {@code ShipSetting} object to the db.
     *
     * @param dto is of {@code ShippingDto} which contains the
     *            necessary info to save a {@code Shipping} object.
     * @throws DuplicateException if dto.country() exists.
     * */
    @Transactional
    public void create(final ShippingDto dto) {
        try {
            repository
                .save(new ShipSetting(dto.country().toLowerCase().trim(), dto.ngn(), dto.usd()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("%s exists".formatted(dto.country()));
        }
    }

    /**
     * Updates a {@code ShipSetting} object.
     *
     * @param dto is of {@code ShippingMapper} which contains the
     *            necessary info to update a {@code Shipping} object.
     * @throws DuplicateException if dto.country() exists.
     * */
    @Transactional
    public void update(final ShippingMapper dto) {
        try {
            repository
                    .updateShipSettingById(
                            dto.id(),
                            dto.country().toLowerCase().trim(),
                            dto.ngn(),
                            dto.usd()
                    );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("%s exists".formatted(dto.country()));
        }
    }

    /**
     * Deletes a {@code ShipSetting} by its primary key.
     *
     * @param id is a primary key for a {@code Shipping} object.
     * @throws ResourceAttachedException if id is equal to 1.
     * */
    @Transactional
    public void delete(final long id) {
        if (id == 1)
            throw new ResourceAttachedException("cannot delete default country.");
        repository.deleteShipSettingById(id);
    }

    public ShipSetting shippingByCountryElseReturnDefault(String country) {
        return repository
                .shippingByCountryElseReturnDefault(country)
                .orElseThrow(() -> {
                    log.error("shipping country does not exist");
                    return new CustomNotFoundException(
                            "country to ship to is not allowed. Please reach out to our customer service."
                    );
                });
    }

}
