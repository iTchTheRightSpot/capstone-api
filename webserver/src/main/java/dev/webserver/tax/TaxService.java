package dev.webserver.tax;

import dev.webserver.exception.CustomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TaxService {

    private static final Logger log = LoggerFactory.getLogger(TaxService.class);

    private final TaxRepository repository;

    public List<TaxDto> taxes() {
        return repository
                .findAll()
                .stream()
                .map(t -> new TaxDto(t.taxId(), t.name(), t.rate()))
                .toList();
    }

    /**
     * Update the default {@link Tax} added in db/migration/V15.
     *
     * @param dto passed from controller.
     * @throws CustomNotFoundException if {@link Tax} percentage
     *                                 isn't in the right format.
     */
    public void update(TaxDto dto) {
        try {
            repository
                    .updateTaxByTaxId(dto.id(), dto.name().toUpperCase().trim(), dto.rate());
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            String error = dto.name().length() > 5
                    ? "%s has to have a max length of 5".formatted(dto.name())
                    : """
                    invalid tax percentage format e.g. 25% tax should be 0.25.
                    Note max of 2 numbers before decimal and 4 numbers after decimal.
                    """;
            throw new CustomNotFoundException(error);
        }
    }

    public Tax taxById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("cannot find tax information"));
    }

}