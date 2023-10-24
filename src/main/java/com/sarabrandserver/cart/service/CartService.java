package com.sarabrandserver.cart.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.product.service.ProductSKUService;
import com.sarabrandserver.user.service.ClientService;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ClientService clientService;
    private final ProductSKUService productSKUService;
    private final CustomUtil customUtil;
    private final S3Service s3Service;

    /**
     * Returns a list of CartResponse based on user principal
     */
    public List<CartResponse> cartItems() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        return this.shoppingSessionRepo.cartItemsByPrincipal(principal) //
                .stream() //
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                    return new CartResponse(pojo.getSession(), url, pojo.getName(), pojo.getSku(), pojo.getQty());
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
    public void create(CartDTO dto) {
        var productSKU = this.productSKUService.productSkuBySKU(dto.sku());

        if (dto.qty() > productSKU.getInventory()) {
            throw new OutOfStockException("chosen quantity is out of stock");
        }

        boolean find = this.shoppingSessionRepo
                .shoppingSessionById(dto.session_id())
                .isPresent();

        String principal = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!find) {
            create_new_shopping_session(principal, dto);
        } else {
            add_to_existing_shopping_session(principal, dto);
        }
    }

    /**
     * Creates a new shopping session
     */
    public void create_new_shopping_session(String principal, CartDTO dto) {
        var date = new Date();
        // 14 hrs from date
        var expireAt = Duration.ofMillis(Duration.ofHours(12).toMillis());

        this.clientService
                .userByPrincipal(principal)
                .ifPresent(user -> {
                    var shoppingSession = ShoppingSession
                            .builder()
                            .createAt(this.customUtil.toUTC(date))
                            .expireAt(this.customUtil.toUTC(new Date(date.getTime() + expireAt.toMillis())))
                            .sarreBrandUser(user)
                            .cartItems(new HashSet<>())
                            .build();

                    var session = this.shoppingSessionRepo.save(shoppingSession);

                    var cartItem = new CartItem(dto.qty(), dto.sku(), session);

                    this.cartItemRepo.save(cartItem);
                });
    }

    /**
     * Update existing session
     */
    public void add_to_existing_shopping_session(String principal, CartDTO dto) {
        var session = this.shoppingSessionRepo
                .shoppingSessionById(dto.session_id())
                .orElseThrow(() -> new CustomNotFoundException("invalid shopping session"));

        Optional<CartItem> cartItem = this.cartItemRepo
                .cartItemBySKUAndPrincipal(dto.sku(), principal);

        // update quantity if cart is present
        if (cartItem.isPresent()) {
            this.cartItemRepo.updateCartQtyByCartId(cartItem.get().getCartId(), dto.qty());
        } else {
            // create new cart
            var cart = new CartItem(dto.qty(), dto.sku(), session);
            this.cartItemRepo.save(cart);
        }

        // TODO update session expiry date
//        this.shoppingSessionRepo
//                .updateSessionExpiry(session.getShoppingSessionId(), this.customUtil.toUTC(da));
    }

    /**
     * Deletes/Removes an item from shopping session
     *
     * @param id ShoppingSession primary key
     * @param sku unique ProductSku
     * */
    @Transactional
    public void remove_from_cart(long id, String sku) {
        this.cartItemRepo.deleteByCartSku(id, sku);
    }

}