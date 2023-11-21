package com.sarabrandserver.cart.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.product.service.ProductSKUService;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductSKUService productSKUService;
    private final CustomUtil customUtil;
    private final S3Service s3Service;

    /**
     * Returns a list of CartResponse based on user principal and currency
     */
    public List<CartResponse> cartItems(String ipAddress, SarreCurrency currency) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");
        return this.shoppingSessionRepo
                .cartItemsByIpAddress(currency, ipAddress) //
                .stream() //
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                    return new CartResponse(
                            pojo.getUuid(),
                            url,
                            pojo.getName(),
                            pojo.getPrice(),
                            pojo.getCurrency(),
                            pojo.getColour(),
                            pojo.getSize(),
                            pojo.getSku(),
                            pojo.getQty()
                    );
                }) //
                .toList();
    }

    /**
     * Creates a new shopping session or persists details into an existing shopping session
     *
     * @throws CustomNotFoundException if dto property sku does not exist
     * @throws OutOfStockException if dto property qty is greater than inventory
     */
    @Transactional
    public void create(String ipAddress, CartDTO dto) {
        var productSKU = this.productSKUService.productSkuBySKU(dto.sku());

        log.info("My custom IpAddress {}", ipAddress);

        if (dto.qty() > productSKU.getInventory()) {
            throw new OutOfStockException("chosen quantity is out of stock");
        }

        Optional<ShoppingSession> optional = this.shoppingSessionRepo
                .shoppingSessionByIPAddress(ipAddress);

        if (optional.isEmpty()) {
            create_new_shopping_session(ipAddress, dto);
        } else {
            add_to_existing_shopping_session(optional.get(), dto);
        }
    }

    /**
     * Creates a new shopping session
     */
    @Transactional
    public void create_new_shopping_session(String ipAddress, CartDTO dto) {
        var date = new Date();
        // 12 hrs from date
        var expireAt = Duration.ofMillis(Duration.ofHours(12).toMillis());

        var shoppingSession = ShoppingSession
                .builder()
                .ipAddress(ipAddress)
                .createAt(this.customUtil.toUTC(date))
                .expireAt(this.customUtil.toUTC(new Date(date.getTime() + expireAt.toMillis())))
                .cartItems(new HashSet<>())
                .build();

        var session = this.shoppingSessionRepo.save(shoppingSession);

        this.cartItemRepo.save(new CartItem(dto.qty(), dto.sku(), session));
    }

    /**
     * Creates or updates a CartItem
     */
    public void add_to_existing_shopping_session(ShoppingSession session, CartDTO dto) {
        CartItem cart = this.cartItemRepo
                .cart_item_by_shopping_session_id_and_sku(session.getShoppingSessionId(), dto.sku())
                .orElse(null);

        if (cart == null) {
            // create new cart
            this.cartItemRepo.save(new CartItem(dto.qty(), dto.sku(), session));
        } else {
            // update quantity if cart is present
            this.cartItemRepo.updateCartQtyByCartId(cart.getCartId(), dto.qty());
        }

        // TODO update session expiry date
//        this.shoppingSessionRepo
//                .updateSessionExpiry(session.getShoppingSessionId(), this.customUtil.toUTC(da));
    }

    /**
     * Deletes a CartItem from ShoppingSession based on
     * sku and user ip address
     *
     * @param sku unique ProductSku
     * */
    @Transactional
    public void remove_from_cart(String ipAddress, String sku) {
        this.cartItemRepo.delete_CartItem_by_ip_and_sku(ipAddress, sku);
    }

}