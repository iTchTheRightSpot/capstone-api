package com.sarabrandserver.payment.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.checkout.CheckoutService;
import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.payment.entity.OrderReservation;
import com.sarabrandserver.payment.repository.OrderReservationRepo;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.thirdparty.ThirdPartyPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.sarabrandserver.enumeration.ReservationStatus.PENDING;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
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

        var expire = new Date(now.plus(15, MINUTES).toEpochMilli());
        var reservations = Set.of(
                new OrderReservation(
                        1L,
                        UUID.randomUUID().toString(),
                        5,
                        PENDING,
                        expire,
                        sku,
                        session
                )
        );
        session.setReservations(reservations);

        var items = Set.of(
                new CartItem(
                        1L,
                        11,
                        session,
                        sku
                )
        );
        session.setCartItems(items);

        // then
        assertThrows(OutOfStockException.class,
                () -> raceConditionService
                        .onPendingReservationsNotEmpty(
                                "",
                                session,
                                date,
                                reservations
                                        .stream()
                                        .collect(Collectors
                                                .toMap(reservation -> reservation
                                                                .getProductSku()
                                                                .getSku(),
                                                        orderReservation -> orderReservation
                                                ))
                                ,
                                items.stream().toList()
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

        var expire = new Date(now.plus(15, MINUTES).toEpochMilli());
        var reservations = Set.of(
                new OrderReservation(
                        1L,
                        UUID.randomUUID().toString(),
                        5,
                        PENDING,
                        expire,
                        sku,
                        session
                ),
                new OrderReservation(
                        2L,
                        UUID.randomUUID().toString(),
                        2,
                        PENDING,
                        expire,
                        sku1,
                        session
                )
        );
        sku.setReservations(reservations);
        session.setReservations(reservations);

        var items = Set.of(
                new CartItem(
                        1L,
                        5,
                        session,
                        sku
                ),
                new CartItem(
                        2L,
                        2,
                        session,
                        sku1
                )
        );
        sku1.setCartItems(items);
        session.setCartItems(items);

        // then
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservations
                                .stream()
                                .collect(Collectors
                                        .toMap(reservation -> reservation
                                                        .getProductSku()
                                                        .getSku(),
                                                orderReservation -> orderReservation
                                        ))
                        ,
                        items.stream().toList()
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
        verify(reservationRepo, times(0)).save(any(OrderReservation.class));

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());

        verify(reservationRepo, times(0))
                .deleteOrderReservationByReservationId(anyLong());
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

        var expire = new Date(now.plus(15, MINUTES).toEpochMilli());
        var reservations = Set.of(
                new OrderReservation(
                        1L,
                        UUID.randomUUID().toString(),
                        8,
                        PENDING,
                        expire,
                        sku,
                        session
                )
        );
        session.setReservations(reservations);

        // reduce qty and adding new item to cart
        var items = Set.of(
                new CartItem(
                        1L,
                        4,
                        session,
                        sku
                ),
                new CartItem(
                        2L,
                        2,
                        session,
                        sku1
                )
        );
        session.setCartItems(items);

        // then
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservations
                                .stream()
                                .collect(Collectors
                                        .toMap(reservation -> reservation
                                                        .getProductSku()
                                                        .getSku(),
                                                orderReservation -> orderReservation
                                        ))
                        ,
                        items.stream().toList()
                );

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
        verify(reservationRepo, times(1)).save(any(OrderReservation.class));

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0))
                .deleteOrderReservationByReservationId(anyLong());
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

        var expire = new Date(now.plus(15, MINUTES).toEpochMilli());
        var reservations = Set.of(
                new OrderReservation(
                        1L,
                        UUID.randomUUID().toString(),
                        3,
                        PENDING,
                        expire,
                        sku,
                        session
                )
        );
        session.setReservations(reservations);

        // reduce qty and adding new item to cart
        var items = Set.of(
                new CartItem(
                        1L,
                        7,
                        session,
                        sku
                ),
                new CartItem(
                        2L,
                        2,
                        session,
                        sku1
                )
        );
        session.setCartItems(items);

        // then
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservations
                                .stream()
                                .collect(Collectors
                                        .toMap(reservation -> reservation
                                                        .getProductSku()
                                                        .getSku(),
                                                orderReservation -> orderReservation
                                        ))
                        ,
                        items.stream().toList()
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
        verify(reservationRepo, times(1)).save(any(OrderReservation.class));

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0))
                .deleteOrderReservationByReservationId(anyLong());
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

        var expire = new Date(now.plus(15, MINUTES).toEpochMilli());
        var reservations = Set.of(
                new OrderReservation(
                        1L,
                        UUID.randomUUID().toString(),
                        3,
                        PENDING,
                        expire,
                        sku,
                        session
                ),
                new OrderReservation(
                        2L,
                        UUID.randomUUID().toString(),
                        3,
                        PENDING,
                        expire,
                        sku1,
                        session
                )
        );
        session.setReservations(reservations);

        // then
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        date,
                        reservations
                                .stream()
                                .collect(Collectors
                                        .toMap(reservation -> reservation
                                                        .getProductSku()
                                                        .getSku(),
                                                orderReservation -> orderReservation
                                        ))
                        ,
                        new ArrayList<>()
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
        verify(reservationRepo, times(0)).save(any(OrderReservation.class));

        verify(skuRepo, times(2))
                .updateProductSkuInventoryByAddingToExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(2))
                .deleteOrderReservationByReservationId(anyLong());
    }

}