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
import com.sarabrandserver.user.service.ClientService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ConcurrentHashMap<String, List<CartItem>> map = new ConcurrentHashMap<>();

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ClientService clientService;
    private final ProductSKUService productSKUService;
    private final CustomUtil customUtil;
    private final S3Service s3Service;

    private String principal() {
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (object instanceof UserDetails) {
            return ((UserDetails) object).getUsername();
        } else if (object instanceof Jwt) {
            return ((Jwt) object).getClaimAsString("sub");
        }

        return "";
    }

    /**
     * Returns a list of CartResponse based on user principal and currency
     */
    public List<CartResponse> cartItems(HttpServletRequest request, SarreCurrency currency) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");
        String principal = principal();

        // TODO impl user does not exist
        return principal.isEmpty()
                ? List.of()
                : this.shoppingSessionRepo.cartItemsByPrincipal(currency, principal) //
                .stream() //
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                    return new CartResponse(
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
     * Creates a shopping session for a user who does not exist
     *
     * @param ip is the users IP address
     */
    private void anonymous_user(String ip, CartDTO dto) {
        // user has an existing session based on IP
        if (this.map.containsKey(ip)) {
            List<CartItem> list = this.map.get(ip);

            var optional = list.stream()
                    .filter(cartItem -> cartItem.getSku().equals(dto.sku()))
                    .findFirst();

            if (optional.isPresent()) {
                optional.get().setQty(dto.qty());
            } else {
                list.add(new CartItem(dto.qty(), dto.sku()));
            }
        } else {
            this.map.put(ip, List.of(new CartItem(dto.qty(), dto.sku())));
        }
    }

    /**
     * Creates a new shopping session or persists details into an existing shopping session
     *
     * @throws CustomNotFoundException if dto property sku does not exist
     * @throws OutOfStockException if dto property qty is greater than inventory
     */
    @Transactional
    public void create(HttpServletRequest request, CartDTO dto) {
        var productSKU = this.productSKUService.productSkuBySKU(dto.sku());

        if (dto.qty() > productSKU.getInventory()) {
            throw new OutOfStockException("chosen quantity is out of stock");
        }

        String principal = principal();

        if (principal.isEmpty()) {
            anonymous_user(request.getRemoteAddr(), dto);
            return;
        }

        Optional<ShoppingSession> optional = this.shoppingSessionRepo
                .shoppingSessionByUserPrincipal(principal);

        if (optional.isEmpty()) {
            create_new_shopping_session(principal, dto);
        } else {
            add_to_existing_shopping_session(optional.get(), dto);
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

                    this.cartItemRepo.save(new CartItem(dto.qty(), dto.sku(), session));
                });
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
     * sku and user principal
     *
     * @param sku unique ProductSku
     * */
    @Transactional
    public void remove_from_cart(HttpServletRequest request, String sku) {
        String principal = principal();

        if (principal.isEmpty()) {
//            List<CartItem> items = this.map.get(request.getRemoteAddr());
//            if (items != null) {
//                items.removeIf(cartItem -> cartItem.getSku().equals(sku));
//            }

            // commented line above is identical
            this.map.computeIfPresent(request.getRemoteAddr(), (key, items) -> items
                    .stream()
                    .filter(cartItem -> !cartItem.getSku().equals(sku))
                    .toList()
            );
        } else {
            this.cartItemRepo.deleteByCartSku(principal, sku);
        }
    }

}