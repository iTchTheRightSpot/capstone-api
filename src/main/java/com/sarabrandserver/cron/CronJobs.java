package com.sarabrandserver.cron;

import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;

@Service
@RequiredArgsConstructor
class CronJobs {

    private final ProductSkuRepo skuRepo;
    private final OrderReservationRepo reservationRepo;
    private final ShoppingSessionRepo sessionRepo;
    private final CartItemRepo cartItemRepo;

    /**
     * Schedule deletion for expired ShoppingSession every 15 mins.
     * Reference
     * <a href="https://docs.spring.io/spring-framework/reference/integration/scheduling.html">documentation</a>
     * */
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES, zone = "UTC")
    @Transactional
    public void schedule() {
        scheduleDeleteShoppingSession();
        scheduleOrderReservationsDeletion();
    }

    /**
     * Delete {@link OrderReservation} that have exceeded 24 hrs.
     * */
    @Transactional
    public void scheduleOrderReservationsDeletion() {
        Date date = CustomUtil
                .toUTC(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));

        reservationRepo.allPendingExpiredReservations(date, PENDING)
                .forEach(reservation -> {
                    skuRepo.updateProductSkuInventoryByAddingToExistingInventory(
                            reservation.getProductSku().getSku(), reservation.getQty());
                    reservationRepo
                            .deleteOrderReservationByReservationId(reservation.getReservationId());
                });
    }

    /**
     * Deletes all expired {@link ShoppingSession} instances and their associated {@link CartItem}.
     * This method retrieves all expired {@link ShoppingSession} instances from the repository
     * using the current date as a reference point. For each expired session, it deletes all
     * {@link CartItem} associated with that session and then deletes the session itself.
     */
    @Transactional
    public void scheduleDeleteShoppingSession() {
        sessionRepo.allExpiredShoppingSession(CustomUtil.toUTC(new Date()))
                .forEach(session -> {
                    this.cartItemRepo
                            .deleteCartItemsByShoppingSessionId(session.shoppingSessionId());
                    this.sessionRepo.deleteById(session.shoppingSessionId());
                });
    }

}
