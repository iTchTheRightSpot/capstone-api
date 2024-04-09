package dev.webserver.payment.service;

import dev.webserver.AbstractUnitTest;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.cart.repository.CartItemRepo;
import dev.webserver.checkout.CheckoutService;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.exception.OutOfStockException;
import dev.webserver.payment.RaceConditionHelper;
import dev.webserver.payment.projection.OrderReservationPojo;
import dev.webserver.payment.projection.RaceConditionCartPojo;
import dev.webserver.payment.repository.OrderReservationRepo;
import dev.webserver.product.entity.ProductSku;
import dev.webserver.product.repository.ProductSkuRepo;
import dev.webserver.thirdparty.ThirdPartyPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RaceConditionServiceTest extends AbstractUnitTest {

    private RaceConditionService raceConditionService;

    @Mock
    private ProductSkuRepo skuRepo;
    @Mock
    private CartItemRepo cartItemRepo;
    @Mock
    private OrderReservationRepo reservationRepo;
    @Mock
    private ThirdPartyPaymentService thirdPartyService;
    @Mock
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        raceConditionService = new RaceConditionService(
                skuRepo,
                cartItemRepo,
                reservationRepo,
                thirdPartyService,
                checkoutService
        );
    }

    @Test
    void errorThrownDueToCartQtyGreaterThanProductSkuInventory() {
        // given
        var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var date = new Date();
        Instant now = Instant.now();
        var session = new ShoppingSession(
                1L,
                "cookie",
                date,
                new Date(now.plus(1, HOURS).toEpochMilli()),
                new HashSet<>(),
                new HashSet<>()
        );

        var list = List.of(
                RaceConditionHelper.raceConditionCartPojo(
                        1L,
                        sku.getSku(),
                        sku.getInventory(),
                        sku.getSize(),
                        1L,
                        15,
                        1L
                )
        );

        Map<String, OrderReservationPojo> map = list.stream()
                .collect(Collectors.toMap(RaceConditionCartPojo::getProductSkuSku,
                        pojo -> RaceConditionHelper.reservationPojo(
                                1L, pojo.getCartItemQty(), pojo.getProductSkuSku())));
        // then
        assertThrows(OutOfStockException.class,
                () -> raceConditionService
                        .onPendingReservationsNotEmpty(
                                "",
                                session,
                                date,
                                map,
                                list
                        )
        );
    }

    @Test
    void onPendingReservationsUserDidNotAddExtraProductSkuToTheirCartOrUpdateQtyOfAnItemInCart() {
        // given
        var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .orderDetails(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var date = new Date();
        Instant now = Instant.now();
        var session = new ShoppingSession(
                1L,
                "cookie",
                date,
                new Date(now.plus(1, HOURS).toEpochMilli()),
                new HashSet<>(),
                new HashSet<>()
        );

        var cartItems = List.of(
                RaceConditionHelper.raceConditionCartPojo(
                        1L,
                        sku.getSku(),
                        sku.getInventory(),
                        sku.getSize(),
                        1L,
                        5,
                        1L
                ),
                RaceConditionHelper.raceConditionCartPojo(
                        1L,
                        sku1.getSku(),
                        sku1.getInventory(),
                        sku1.getSize(),
                        1L,
                        2,
                        1L
                )
        );

        var reservations = cartItems.stream()
                .collect(Collectors.toMap(RaceConditionCartPojo::getProductSkuSku,
                        pojo -> RaceConditionHelper.reservationPojo(
                                1L, pojo.getCartItemQty(), pojo.getProductSkuSku())));

        // method to test
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservations,
                        cartItems
                );

        // then
        verify(reservationRepo, times(0))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(0))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(0))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(Date.class), anyLong(), anyLong());

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());

        verify(reservationRepo, times(0)).deleteById(anyLong());
    }

    @Test
    void userAddedAnExtraItemToTheirCartAndAlsoDecreasedTheQtyOfAnExistingItem() {
        // given
        var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var date = new Date();
        Instant now = Instant.now();
        var session = new ShoppingSession(
                1L,
                "cookie",
                date,
                new Date(now.plus(1, HOURS).toEpochMilli()),
                new HashSet<>(),
                new HashSet<>()
        );

        var cartItems = List.of(
                RaceConditionHelper.raceConditionCartPojo(
                        sku.getSkuId(),
                        sku.getSku(),
                        sku.getInventory(),
                        sku.getSize(),
                        1L,
                        3,
                        1L
                ),
                RaceConditionHelper.raceConditionCartPojo(
                        sku1.getSkuId(),
                        sku1.getSku(),
                        sku1.getInventory(),
                        sku1.getSize(),
                        2L,
                        4,
                        1L
                )
        );

        var reservation = Stream.of(sku)
                .collect(Collectors.toMap(ProductSku::getSku,
                        s -> RaceConditionHelper.reservationPojo(1L, 7, s.getSku())));

        // method to test
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservation,
                        cartItems
                );

        // then
        verify(reservationRepo, times(1))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(0))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(1))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(1))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(Date.class), anyLong(), anyLong());

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0)).deleteById(anyLong());
    }

    @Test
    void userAddedAnExtraItemToTheirCartAndIncreasedTheQtyOfAnExistingItem() {
        // given
        var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var date = new Date();
        Instant now = Instant.now();
        var session = new ShoppingSession(
                1L,
                "cookie",
                date,
                new Date(now.plus(1, HOURS).toEpochMilli()),
                new HashSet<>(),
                new HashSet<>()
        );

        var cartItems = List.of(
                RaceConditionHelper.raceConditionCartPojo(
                        sku.getSkuId(),
                        sku.getSku(),
                        sku.getInventory(),
                        sku.getSize(),
                        1L,
                        7,
                        1L
                ),
                RaceConditionHelper.raceConditionCartPojo(
                        sku1.getSkuId(),
                        sku1.getSku(),
                        sku1.getInventory(),
                        sku1.getSize(),
                        1L,
                        4,
                        1L
                )

        );

        var reservation = Stream.of(sku)
                .collect(Collectors.toMap(ProductSku::getSku,
                        s -> RaceConditionHelper.reservationPojo(1L, 3, s.getSku())));

        // then
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservation,
                        cartItems
                );

        verify(reservationRepo, times(0))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(1))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(1))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(1))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(Date.class), anyLong(), anyLong());

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0)).deleteById(anyLong());
    }

    @Test
    void updateProductSkuInventoryBecauseUserDeletesAllItemsFromCart() {
        // given
        var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .orderDetails(new HashSet<>())
                .reservations(new HashSet<>())
                .cartItems(new HashSet<>())
                .build();

        var date = new Date();
        Instant now = Instant.now();
        var session = new ShoppingSession(
                1L,
                "cookie",
                date,
                new Date(now.plus(1, HOURS).toEpochMilli()),
                new HashSet<>(),
                new HashSet<>()
        );

        var reservations = Stream.of(sku, sku1)
                .collect(Collectors.toMap(ProductSku::getSku,
                        s -> RaceConditionHelper.reservationPojo(1L, s.getInventory(), s.getSku())));

        // method to test
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservations,
                        List.of()
                );

        // then
        verify(reservationRepo, times(0))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(0))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(Date.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(0))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(Date.class), anyLong(), anyLong());

        verify(skuRepo, times(2))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(2)).deleteById(anyLong());
    }

}