package com.sarabrandserver.shipping;

import com.sarabrandserver.enumeration.ShippingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepo repository;

    public List<ShippingResponse> allShipping() {
        return repository.findAll()
                .stream()
                .map(s -> new ShippingResponse(s.shippingId(), s.ngnPrice(), s.usdPrice(), s.type()))
                .toList();
    }

    public void create(ShippingDto dto) {
        ShippingType type = ShippingType
                .valueOf(dto.type().toUpperCase());
        repository.save(new Shipping(dto.ngn(), dto.usd(), type));
    }

    public void delete(long shippingId) {
        repository.deleteByShippingId(shippingId);
    }

    public void update(ShippingUpdateDto dto) {
        repository.updateByShippingId(dto.id(), dto.ngn(), dto.usd());
    }

}
