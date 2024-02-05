package com.sarabrandserver.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.payment.dto.OrderHistoryDTO;
import com.sarabrandserver.payment.dto.PayloadMapper;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    @Setter
    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final OrderDetailRepository repository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    public List<OrderHistoryDTO> orderHistory() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.repository
                .orderHistoryByPrincipal(principal)
                .stream()
                .map(p -> {
                    var detail = transform(objectMapper, s3Service, BUCKET, p.getDetail());
                    return new OrderHistoryDTO(
                            p.getTime().getTime(),
                            p.getCurrency(),
                            p.getTotal(),
                            p.getPaymentId(),
                            detail
                    );
                })
                .toList();
    }

    /**
     * Maps from string to PayloadMapper[]
     * param str is in format [ { "name" : "", "key" : "", "colour" : "" } ]
     * */
    public static PayloadMapper[] transform(ObjectMapper mapper, S3Service s3Service, String bucketName, String str) {
        try {
            PayloadMapper[] arr = mapper.readValue(str, PayloadMapper[].class);
            return Arrays.stream(arr)
                    .map(m -> {
                        String url = s3Service.preSignedUrl(bucketName, m.key());
                        return new PayloadMapper(m.name(), url, m.colour());
                    })
                    .toArray(PayloadMapper[]::new);
        } catch (JsonProcessingException e) {
            log.error("error converting str to PayloadMapper");
            return null;
        }
    }

}
