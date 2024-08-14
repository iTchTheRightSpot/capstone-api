package dev.webserver.shipping;

import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the logic for countries we are allowed to ship to.
 * */
@Service
@RequiredArgsConstructor
class ShippingService {

    private final ShippingRepository repository;

    public List<ShippingMapper> shipping() {
        final List<ShippingMapper> list = new ArrayList<>();
        for (final ShipSetting ship : repository.findAll())
            list.add(new ShippingMapper(ship.shipId(), ship.country(), ship.ngnPrice(), ship.usdPrice()));
        return list;
    }

    /**
     * Saves a {@link ShipSetting} object to the db.
     *
     * @param dto is of {@link ShippingDto} which contains the
     *            necessary info to save a {@link ShipSetting} object.
     * @throws DuplicateException if dto.country() exists.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void create(final ShippingDto dto) {
        try {
            repository
                .save(new ShipSetting(null, dto.country().toLowerCase().trim(), dto.ngn(), dto.usd()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("%s exists".formatted(dto.country()));
        }
    }

    /**
     * Updates a {@link ShipSetting} object.
     *
     * @param dto is of {@link ShippingMapper} which contains the
     *            necessary info to update a {@link ShipSetting} object.
     * @throws DuplicateException if dto.country() exists.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void update(final ShippingMapper dto) {
        try {
            repository.updateShipSettingById(
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
     * Deletes a {@link ShipSetting} by its primary key.
     *
     * @param id is a primary key for a {@link ShipSetting} object.
     * @throws ResourceAttachedException if categoryId is equal to 1.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void delete(final long id) {
        if (id == 1)
            throw new ResourceAttachedException("cannot delete default country.");
        repository.deleteShipSettingById(id);
    }

}
