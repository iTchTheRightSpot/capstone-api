package dev.webserver.tax;

import dev.webserver.exception.CustomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
class TaxService {

    private static final Logger log = LoggerFactory.getLogger(TaxService.class);

    private final TaxRepository repository;

    public List<TaxDto> taxes() {
        final List<TaxDto> list = new ArrayList<>();
        for (final Tax tax : repository.findAll())
            list.add(new TaxDto(tax.taxId(), tax.name(), tax.rate()));
        return list;
    }

    /**
     * Update the default {@link Tax} added in db/migration/V15.
     *
     * @param dto passed from controller.
     * @throws CustomNotFoundException if {@link Tax} percentage
     *                                 isn't in the right format.
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(final TaxDto dto) {
        try {
            repository
                    .updateTaxByTaxId(dto.id(), dto.name().toUpperCase().trim(), dto.rate());
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            final String error = dto.name().length() > 5
                    ? "%s has to have a max length of 5".formatted(dto.name())
                    : """
                    invalid tax percentage format e.g. 25% tax should be 0.25.
                    Note max of 2 numbers before decimal and 4 numbers after decimal.
                    """;
            throw new CustomNotFoundException(error);
        }
    }

}