package com.sarabrandserver.cron;

import com.sarabrandserver.AbstractIntegration;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.product.entity.ProductSku;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CronJobsTest extends AbstractIntegration {

    @Autowired
    private CronJobs cronJobs;

    @Test
    void validateOrderReservationsFromPaystack() {
        final String ref0 = "ref-92318272839885";
        final String ref1 = "ref-88387376426331";

        var list = List.of(
                new OrderReservation(ref0, 1,ReservationStatus.PENDING,
                        new Date(), new ProductSku(), new ShoppingSession()),
                new OrderReservation(ref1, 1,ReservationStatus.PENDING,
                        new Date(), new ProductSku(), new ShoppingSession())
        );

        List<OrderReservation> reservations = cronJobs
                .validateOrderReservationsFromPaystack(list);

        assertEquals(0, reservations.size());
    }

}