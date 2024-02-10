package com.sarabrandserver.cron;

import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;

@Configuration
@RequiredArgsConstructor
public class CronJobs {

    private final ProductSkuRepo skuRepo;
    private final OrderReservationRepo reservationRepo;
    private final ShoppingSessionRepo sessionRepo;
    private final CartItemRepo cartItemRepo;

    /**
     * Schedule deletion for expired ShoppingSession every 10 mins
     * <a href="https://docs.spring.io/spring-framework/reference/integration/scheduling.html">...</a>
     * */
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES, zone = "UTC")
    @Transactional
    public void schedule() {
        scheduleDeleteShoppingSession();
        scheduleDeleteOrderReservations();
    }

    /**
     * Returning pending order reservation back to stock.
     * Note we are making the interval to be 20 mins
     * for extra breathing space from payment provider webhook
     * */
    @Transactional
    public void scheduleDeleteOrderReservations() {
        Date date = CustomUtil.toUTC(new Date());
        var list = this.reservationRepo
                .allPendingExpiredReservations(date, PENDING);

        for (OrderReservation r : list) {
            this.skuRepo
                    .updateProductSkuInventoryByAddingToExistingInventory(r.getProductSku().getSku(), r.getQty());
            this.reservationRepo
                    .deleteOrderReservationByReservationId(r.getReservationId());
        }
        this.reservationRepo.deleteExpiredOrderReservations(date, PENDING);
    }

    /**
     * Deletes all expired {@code ShoppingSession} instances and their associated {@code CartItems}.
     * This method retrieves all expired {@code ShoppingSession} instances from the repository
     * using the current date as a reference point. For each expired session, it deletes all
     * {@code CartItems} associated with that session and then deletes the session itself.
     */
    @Transactional
    public void scheduleDeleteShoppingSession() {
        var list = this.sessionRepo
                .allExpiredShoppingSession(CustomUtil.toUTC(new Date()));
        for (ShoppingSession s : list) {
            this.cartItemRepo
                    .deleteCartItemsByShoppingSessionId(s.shoppingSessionId());
            this.sessionRepo.deleteById(s.shoppingSessionId());
        }
    }

}
