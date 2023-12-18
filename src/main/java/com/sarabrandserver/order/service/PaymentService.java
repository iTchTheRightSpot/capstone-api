package com.sarabrandserver.order.service;

import com.sarabrandserver.address.Address;
import com.sarabrandserver.address.AddressDTO;
import com.sarabrandserver.address.AddressRepo;
import com.sarabrandserver.enumeration.PaymentStatus;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.order.dto.PaymentDTO;
import com.sarabrandserver.order.dto.SkuQtyDTO;
import com.sarabrandserver.order.entity.OrderDetail;
import com.sarabrandserver.order.entity.PaymentDetail;
import com.sarabrandserver.order.repository.OrderRepository;
import com.sarabrandserver.order.repository.PaymentRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProductSkuRepo productSkuRepo;
    private final AddressRepo addressRepo;
    private final PaymentRepo paymentRepo;
    private final OrderRepository orderRepo;
    private final CustomUtil customUtil;

    public Page<?> orders(int page, int min) {
        return null;
    }

    /**
     * Test implementation for purchasing products
     * */
    @Transactional
    public void order(final PaymentDTO dto, final AddressDTO dto1) {
        for (SkuQtyDTO obj : dto.dto()) {
            this.productSkuRepo.updateInventory(obj.sku(), obj.qty());
        }

        Date date = this.customUtil.toUTC(new Date());

        // currency
        var currency = SarreCurrency.valueOf(dto.currency());

        // save PaymentDetail
        var payment = PaymentDetail.builder()
                .email(dto.email())
                .name(dto.name())
                .phone(dto.phone())
                .payment_id(UUID.randomUUID().toString())
                .currency(currency)
                .amount(dto.total())
                .paymentProvider(dto.paymentProvider())
                .paymentStatus(PaymentStatus.CONFIRMED)
                .createAt(date)
                .orderDetail(new HashSet<>())
                .build();

        var savedPayment = this.paymentRepo.save(payment);

        // save OrderDetail
        for (SkuQtyDTO obj : dto.dto()) {
            this.orderRepo.save(new OrderDetail(obj.sku(), obj.qty(), savedPayment));
        }

        // save Address
        var address = Address.builder()
                .address(dto1.address())
                .city(dto1.city())
                .state(dto1.state())
                .postcode(dto1.postcode())
                .country(dto1.country())
                .deliveryInfo(dto1.deliveryInfo())
                .paymentDetail(savedPayment)
                .build();

        this.addressRepo.save(address);
    }

}