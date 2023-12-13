package com.sarabrandserver.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.order.dto.OrderHistoryDTO;
import com.sarabrandserver.order.dto.PayloadMapper;
import com.sarabrandserver.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class.getName());

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final OrderRepository orderRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    public List<OrderHistoryDTO> orderHistory() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.orderRepository
                .orderHistoryByPrincipal(principal)
                .stream()
                .map(p -> {
                    var m = transform(p.getDetail());
                    return new OrderHistoryDTO(p.getTime().getTime(), p.getTotal(), p.getPaymentId(), m);
                })
                .toList();
    }

    /**
     * Maps from string to PayloadMapper[]
     * param str is in format [ "name" : "", "key" : "", "qty" : "" ]
     * */
    private PayloadMapper[] transform(String str) {
        try {
            PayloadMapper[] arr = this.objectMapper.readValue(str, PayloadMapper[].class);
            return Arrays.stream(arr)
                    .map(m -> {
                        String url = this.s3Service.preSignedUrl(this.BUCKET, m.key());
                        return new PayloadMapper(m.name(), url, m.colour(), m.qty());
                    })
                    .toArray(PayloadMapper[]::new);
        } catch (JsonProcessingException e) {
            log.error("Error converting from PayloadDTO to OrderHistoryDTO");
            return null;
        }
    }

}
