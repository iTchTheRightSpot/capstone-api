package com.sarabrandserver.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.exception.CustomServerError;
import com.sarabrandserver.payment.dto.OrderHistoryDTO;
import com.sarabrandserver.payment.dto.PayloadMapper;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class.getName());

    @Setter
    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final OrderDetailRepository repository;
    private final S3Service s3Service;

    /**
     * Retrieves the order history asynchronously for the currently authenticated user.
     * <p>
     * This method first retrieves a list of transformed {@link com.sarabrandserver.payment.entity.OrderDetail}
     * to {@link com.sarabrandserver.payment.projection.OrderPojo} from the database based on the authenticated
     * user's principal, then processes each order to create a list of {@link CompletableFuture} tasks.
     * Each {@link CompletableFuture} task reads the {@link com.sarabrandserver.payment.projection.OrderPojo}
     * property getDetail, parses it into a {@link PayloadMapper} array, and asynchronously fetches pre-signed
     * URLs for associated keys from the S3 service. The resulting {@link PayloadMapper} array is then combined
     * with other order details to form an {@link OrderHistoryDTO} object. Finally, all {@link CompletableFuture}
     * tasks are executed concurrently to retrieve the order history efficiently.
     *
     * @return A {@link CompletableFuture} containing a list of {@link OrderHistoryDTO} objects, representing
     * the order history for the currently authenticated user.
     * @throws CustomServerError if an error occurs transforming
     * {@link com.sarabrandserver.payment.projection.OrderPojo} property to a {@link PayloadMapper} array.
     */
    public CompletableFuture<List<OrderHistoryDTO>> orderHistory() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();

        List<CompletableFuture<OrderHistoryDTO>> jobs = repository
                .orderHistoryByPrincipal(principal)
                .stream()
                .map(db -> CompletableFuture.supplyAsync(() -> {
                    try {
                        var array = new ObjectMapper()
                                .readValue(db.getDetail(), PayloadMapper[].class);

                        var supplierList = Arrays.stream(array)
                                .map(a -> (Supplier<PayloadMapper>) () ->
                                        new PayloadMapper(a.name(), s3Service.preSignedUrl(BUCKET, a.key()), a.colour())
                                )
                                .toList();

                        PayloadMapper[] asyncResponse = CustomUtil.asynchronousTasks(supplierList)
                                .thenApply(v -> v.stream().map(Supplier::get).toArray(PayloadMapper[]::new)) //
                                .join();

                        return new OrderHistoryDTO(
                                db.getTime().getTime(),
                                db.getCurrency(),
                                db.getTotal(),
                                db.getPaymentId(),
                                asyncResponse
                        );
                    } catch (JsonProcessingException e) {
                        log.error("error retrieving customer %s order history \n %s"
                                .formatted(principal, e.getMessage())
                        );
                        throw new CustomServerError(
                                """
                                An error occurred retrieving your order history.
                                Please reach out to our customer service.
                                """
                        );
                    }
                }))
                .toList();

        return CustomUtil.asynchronousTasks(jobs)
                .thenApply(v -> jobs.stream().map(CompletableFuture::join).toList());
    }

}
