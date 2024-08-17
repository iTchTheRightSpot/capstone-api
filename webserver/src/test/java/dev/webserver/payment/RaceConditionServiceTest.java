package dev.webserver.payment;

import dev.webserver.AbstractUnitTest;
import dev.webserver.cart.ICartRepository;
import dev.webserver.cart.ShoppingSession;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.exception.OutOfStockException;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

final class RaceConditionServiceTest extends AbstractUnitTest {

    private RaceConditionService raceConditionService;

    @Mock
    private Environment environment;
    @Mock
    private ProductSkuRepository skuRepo;
    @Mock
    private ICartRepository iCartRepository;
    @Mock
    private OrderReservationRepository reservationRepo;
    @Mock
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        raceConditionService = new RaceConditionService(
                environment,
                skuRepo,
                iCartRepository,
                reservationRepo,
                checkoutService
        );
        super.setUpEnvironmentVariables(raceConditionService);
    }

    @Test
    void errorThrownDueToCartQtyGreaterThanProductSkuInventory() {
        // given
        final var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .build();

        final var ldt = LocalDateTime.now();
        final var session = new ShoppingSession(
                1L,
                "cookie",
                ldt,
                ldt.plusHours(1)
        );

        final var list = List.of(
                new RaceConditionCartDbMapper(
                        1L,
                        sku.sku(),
                        sku.inventory(),
                        sku.size(),
                        1L,
                        15,
                        1L
                )
        );

        final Map<String, OrderReservationDbMapper> map = list.stream()
                .collect(Collectors.toMap(RaceConditionCartDbMapper::sku,
                        pojo -> new OrderReservationDbMapper(1L, pojo.qty(), sku.skuId(), pojo.sku())));
        // then
        assertThrows(OutOfStockException.class,
                () -> raceConditionService
                        .onPendingReservationsNotEmpty(
                                "",
                                session,
                                ldt,
                                map,
                                list
                        )
        );
    }

    @Test
    void onPendingReservationsUserDidNotAddExtraProductSkuToTheirCartOrUpdateQtyOfAnItemInCart() {
        // given
        final var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .build();

        final var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .build();

        final var ldt = LocalDateTime.now();
        final var session = new ShoppingSession(
                1L,
                "cookie",
                ldt,
                ldt.plusHours(1)
        );

        final var cartItems = List.of(
                new RaceConditionCartDbMapper(
                        1L,
                        sku.sku(),
                        sku.inventory(),
                        sku.size(),
                        1L,
                        5,
                        1L
                ),
                new RaceConditionCartDbMapper(
                        1L,
                        sku1.sku(),
                        sku1.inventory(),
                        sku1.size(),
                        1L,
                        2,
                        1L
                )
        );

        final var reservations = cartItems.stream()
                .collect(Collectors.toMap(RaceConditionCartDbMapper::sku,
                        pojo -> new OrderReservationDbMapper(1L, pojo.qty(), sku.skuId(), pojo.sku())));

        // method to test
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        ldt,
                        reservations,
                        cartItems
                );

        // then
        verify(reservationRepo, times(0))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(0))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(0))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(LocalDateTime.class), anyLong(), anyLong());

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyLong(), anyInt());

        verify(reservationRepo, times(0)).deleteById(anyLong());
    }

    @Test
    void userAddedAnExtraItemToTheirCartAndAlsoDecreasedTheQtyOfAnExistingItem() {
        // given
        final var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .build();

        final var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .build();

        final var ldt = LocalDateTime.now();
        final var session = new ShoppingSession(
                1L,
                "cookie",
                ldt,
                ldt.plusHours(1)
        );

        final var cartItems = List.of(
                new RaceConditionCartDbMapper(
                        sku.skuId(),
                        sku.sku(),
                        sku.inventory(),
                        sku.size(),
                        1L,
                        3,
                        1L
                ),
                new RaceConditionCartDbMapper(
                        sku1.skuId(),
                        sku1.sku(),
                        sku1.inventory(),
                        sku1.size(),
                        2L,
                        4,
                        1L
                )
        );

        final var reservation = Stream.of(sku)
                .collect(Collectors.toMap(ProductSku::sku, s -> new OrderReservationDbMapper(1L, 7, s.skuId(), s.sku())));

        // method to test
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        ldt,
                        reservation,
                        cartItems
                );

        // then
        verify(reservationRepo, times(1))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(0))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(1))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(1))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(LocalDateTime.class), anyLong(), anyLong());

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyLong(), anyInt());
        verify(reservationRepo, times(0)).deleteById(anyLong());
    }

    // TODO why is sku1 not used?
    @Test
    void userAddedAnExtraItemToTheirCartAndIncreasedTheQtyOfAnExistingItem() {
        // given
        final var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .build();

        final var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .build();

        final var ldt = LocalDateTime.now();
        final var session = new ShoppingSession(
                1L,
                "cookie",
                ldt,
                ldt.plusHours(1)
        );

        final var cartItems = List.of(
                new RaceConditionCartDbMapper(
                        sku.skuId(),
                        sku.sku(),
                        sku.inventory(),
                        sku.size(),
                        1L,
                        7,
                        1L
                ),
                new RaceConditionCartDbMapper(
                        sku.skuId(),
                        sku.sku(),
                        sku.inventory(),
                        sku.size(),
                        1L,
                        4,
                        1L
                )
        );

        final var reservation = Stream.of(sku)
                .collect(Collectors.toMap(ProductSku::sku, s -> new OrderReservationDbMapper(1L, 3, s.skuId(), s.sku())));

        // then
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        ldt,
                        reservation,
                        cartItems
                );

        verify(reservationRepo, times(0))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(1))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(1))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(1))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(LocalDateTime.class), anyLong(), anyLong());

        verify(skuRepo, times(0))
                .updateProductSkuInventoryByAddingToExistingInventory(anyLong(), anyInt());
        verify(reservationRepo, times(0)).deleteById(anyLong());
    }

    @Test
    void updateProductSkuInventoryBecauseUserDeletesAllItemsFromCart() {
        // given
        final var sku = ProductSku.builder()
                .skuId(1L)
                .sku("sku-0")
                .size("medium")
                .inventory(10)
                .build();

        final var sku1 = ProductSku.builder()
                .skuId(2L)
                .sku("sku-1")
                .size("large")
                .inventory(5)
                .build();

        final var ldt = LocalDateTime.now();
        final var session = new ShoppingSession(
                1L,
                "cookie",
                ldt,
                ldt.plusHours(1)
        );

        final var reservations = Stream.of(sku, sku1)
                .collect(Collectors.toMap(ProductSku::sku,
                        s -> new OrderReservationDbMapper(1L, s.inventory(), s.skuId(), s.sku())));

        // method to test
        raceConditionService
                .onPendingReservationsNotEmpty(
                        "",
                        session,
                        ldt,
                        reservations,
                        List.of()
                );

        // then
        verify(reservationRepo, times(0))
                .addToProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );
        verify(reservationRepo, times(0))
                .deductFromProductSkuInventoryAndReplaceReservationQty(
                        anyInt(),
                        anyInt(),
                        anyString(),
                        any(LocalDateTime.class),
                        anyString(),
                        anyString(),
                        any(ReservationStatus.class)
                );

        verify(skuRepo, times(0))
                .updateProductSkuInventoryBySubtractingFromExistingInventory(anyString(), anyInt());
        verify(reservationRepo, times(0))
                .saveOrderReservation(anyString(), anyInt(), any(ReservationStatus.class), any(LocalDateTime.class), anyLong(), anyLong());

        verify(skuRepo, times(2))
                .updateProductSkuInventoryByAddingToExistingInventory(anyLong(), anyInt());
        verify(reservationRepo, times(2)).deleteById(anyLong());
    }

}