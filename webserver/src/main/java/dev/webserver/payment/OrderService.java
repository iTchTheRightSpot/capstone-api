package dev.webserver.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.exception.CustomServerError;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class.getName());

    @Setter
    @Value(value = "${aws.bucket}")
    private String bucket;

    private final OrderDetailRepository repository;
    private final IS3Service s3Service;

    /**
     * Retrieves the order history asynchronously for the currently authenticated user.
     * <p>
     * This method first retrieves a list of transformed {@link OrderDetail}
     * to {@link OrderDetailDbMapper} from the database based on the authenticated
     * user's principal, then processes each order to create a list of deferred tasks.
     * Each task reads the {@link OrderDetailDbMapper} property getDetail, parses it into a {@link OrderHistoryDbMapper}
     * array, and asynchronously fetches pre-signed URLs for associated keys from the S3 service. The resulting
     * {@link OrderHistoryDbMapper} array is then combined  with other order details to form an {@link OrderHistoryDto} object.
     * Finally, all deferred tasks are executed concurrently to retrieve the order history efficiently.
     *
     * @return A list of {@link OrderHistoryDto} objects, representing
     * the order history for the currently authenticated user.
     * @throws CustomServerError if an error occurs transforming
     * {@link OrderDetailDbMapper} property to a {@link OrderHistoryDbMapper} array.
     */
    public List<OrderHistoryDto> orderHistory() {
        final String principal = SecurityContextHolder.getContext().getAuthentication().getName();

        final var jobs = repository
                .orderHistoryByPrincipal(principal)
                .stream()
                .map(db -> (Supplier<OrderHistoryDto>) () -> {
                    try {
                        final var array = new ObjectMapper().readValue(db.detail(), OrderHistoryDbMapper[].class);

                        final var async = Arrays.stream(array)
                                .map(a -> (Supplier<OrderHistoryDbMapper>) () -> new OrderHistoryDbMapper(a.name(), s3Service.preSignedUrl(bucket, a.imageKey()), a.colour()))
                                .toList();

                        return new OrderHistoryDto(
                                db.createdAt().toInstant(ZoneOffset.UTC).toEpochMilli(),
                                db.currency(),
                                db.amount(),
                                db.referenceId(),
                                CustomUtil.asynchronousTasks(async).join().toArray(OrderHistoryDbMapper[]::new)
                        );
                    } catch (JsonProcessingException e) {
                        log.error("error retrieving customer %s order history \n %s".formatted(principal, e.getMessage()));
                        throw new CustomServerError(
                                """
                                An error occurred retrieving your order history.
                                Please reach out to our customer service.
                                """
                        );
                    }
                })
                .toList();

        return CustomUtil.asynchronousTasks(jobs).join();
    }

}