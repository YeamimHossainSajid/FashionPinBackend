package com.fashionpin.shoppingservice.service;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.shoppingservice.client.ProductServiceClient;
import com.fashionpin.shoppingservice.client.ProductServiceClient.ProductDto;
import com.fashionpin.shoppingservice.client.ProductServiceClient.ProductVariantDto;
import com.fashionpin.shoppingservice.dto.AddToCartRequest;
import com.fashionpin.shoppingservice.dto.CartItemDto;
import com.fashionpin.shoppingservice.dto.CartResponse;
import com.fashionpin.shoppingservice.dto.MergeCartRequest;
import com.fashionpin.shoppingservice.dto.UpdateCartItemRequest;
import com.fashionpin.shoppingservice.entity.CartItem;
import com.fashionpin.shoppingservice.entity.PromoCode;
import com.fashionpin.shoppingservice.entity.ShoppingCart;
import com.fashionpin.shoppingservice.repository.CartItemRepository;
import com.fashionpin.shoppingservice.repository.PromoCodeRepository;
import com.fashionpin.shoppingservice.repository.ShoppingCartRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public ShoppingCart resolveCart(String userId, String guestSessionToken) {
        if (userId != null && !userId.isBlank()) {
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> cartRepository.save(ShoppingCart.builder().userId(userId).build()));
        }

        if (guestSessionToken != null && !guestSessionToken.isBlank()) {
            return cartRepository.findByGuestSessionToken(guestSessionToken)
                    .orElseGet(() -> cartRepository.save(ShoppingCart.builder().guestSessionToken(guestSessionToken).build()));
        }

        String newToken = "guest_" + UUID.randomUUID().toString();
        return cartRepository.save(ShoppingCart.builder().guestSessionToken(newToken).build());
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId, String guestSessionToken) {
        ShoppingCart cart = resolveCart(userId, guestSessionToken);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(String userId, String guestSessionToken, AddToCartRequest request) {
        ShoppingCart cart = resolveCart(userId, guestSessionToken);

        // Fetch product snapshot via Feign
        ProductDto product = null;
        try {
            ApiResponse<ProductDto> resp = productServiceClient.getProductById(request.getProductId());
            if (resp != null && resp.getData() != null) {
                product = resp.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch product snapshot from product-service for {}: {}", request.getProductId(), e.getMessage());
        }

        String variantId = request.getVariantId() != null && !request.getVariantId().isBlank()
                ? request.getVariantId()
                : (product != null && product.getVariants() != null && !product.getVariants().isEmpty()
                        ? product.getVariants().get(0).getId()
                        : "var_" + UUID.randomUUID().toString().substring(0, 8));

        ProductVariantDto variant = null;
        if (product != null && product.getVariants() != null) {
            variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(variantId))
                    .findFirst()
                    .orElse(null);
        }

        BigDecimal unitPrice = variant != null && variant.getEffectivePrice() != null
                ? variant.getEffectivePrice()
                : (product != null && product.getPrice() != null ? product.getPrice() : new BigDecimal("99.00"));

        String productName = product != null ? product.getName() : "Fashion Item";
        String color = variant != null ? variant.getColorName() : (product != null ? product.getColor() : null);
        String size = variant != null ? variant.getSize() : (product != null ? product.getSize() : null);
        String sku = variant != null ? variant.getSku() : null;

        Optional<CartItem> existingOpt = cart.getItems().stream()
                .filter(i -> i.getVariantId().equals(variantId))
                .findFirst();

        int qtyToAdd = request.getQuantity() != null ? request.getQuantity() : 1;

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + qtyToAdd);
            existing.setUnitPrice(unitPrice);
            cartItemRepository.save(existing);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .variantId(variantId)
                    .quantity(qtyToAdd)
                    .unitPrice(unitPrice)
                    .currency(product != null && product.getCurrency() != null ? product.getCurrency() : "USD")
                    .productName(productName)
                    .productImage(product != null ? product.getPrimaryMediaId() : null)
                    .color(color)
                    .size(size)
                    .sku(sku)
                    .build();
            cart.addItem(item);
            cartItemRepository.save(item);
        }

        recalculateDiscounts(cart);
        ShoppingCart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse updateCartItem(String userId, String guestSessionToken, String itemId, UpdateCartItemRequest request) {
        ShoppingCart cart = resolveCart(userId, guestSessionToken);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException("UNAUTHORIZED_CART_ACTION", "Item does not belong to user cart");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        recalculateDiscounts(cart);
        ShoppingCart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse removeCartItem(String userId, String guestSessionToken, String itemId) {
        ShoppingCart cart = resolveCart(userId, guestSessionToken);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException("UNAUTHORIZED_CART_ACTION", "Item does not belong to user cart");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);

        recalculateDiscounts(cart);
        ShoppingCart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse applyPromoCode(String userId, String guestSessionToken, String promoCodeStr) {
        ShoppingCart cart = resolveCart(userId, guestSessionToken);

        PromoCode promo = promoCodeRepository.findByCodeIgnoreCaseAndIsActiveTrue(promoCodeStr.trim())
                .orElseThrow(() -> new BusinessException("INVALID_PROMO_CODE", "Promo code is invalid or expired"));

        if (promo.getExpiresAt() != null && promo.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("EXPIRED_PROMO_CODE", "Promo code has expired");
        }

        cart.setPromoCode(promo.getCode());
        recalculateDiscounts(cart);

        ShoppingCart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse removePromoCode(String userId, String guestSessionToken) {
        ShoppingCart cart = resolveCart(userId, guestSessionToken);
        cart.setPromoCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);

        ShoppingCart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse mergeCart(String userId, MergeCartRequest request) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("USER_AUTH_REQUIRED", "User authentication required to merge cart");
        }

        Optional<ShoppingCart> guestCartOpt = cartRepository.findByGuestSessionToken(request.getGuestSessionToken());
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            return getCart(userId, null);
        }

        ShoppingCart guestCart = guestCartOpt.get();
        ShoppingCart userCart = resolveCart(userId, null);

        for (CartItem guestItem : new ArrayList<>(guestCart.getItems())) {
            Optional<CartItem> existingUserItem = userCart.getItems().stream()
                    .filter(i -> i.getVariantId().equals(guestItem.getVariantId()))
                    .findFirst();

            if (existingUserItem.isPresent()) {
                CartItem existing = existingUserItem.get();
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(existing);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(userCart)
                        .productId(guestItem.getProductId())
                        .variantId(guestItem.getVariantId())
                        .quantity(guestItem.getQuantity())
                        .unitPrice(guestItem.getUnitPrice())
                        .currency(guestItem.getCurrency())
                        .productName(guestItem.getProductName())
                        .productImage(guestItem.getProductImage())
                        .color(guestItem.getColor())
                        .size(guestItem.getSize())
                        .sku(guestItem.getSku())
                        .build();
                userCart.addItem(newItem);
                cartItemRepository.save(newItem);
            }
        }

        // Clean up old guest cart
        cartRepository.delete(guestCart);

        recalculateDiscounts(userCart);
        ShoppingCart saved = cartRepository.save(userCart);
        return mapToResponse(saved);
    }

    @Transactional
    public void clearCart(String cartId) {
        cartItemRepository.deleteByCartId(cartId);
        cartRepository.findById(cartId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setPromoCode(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
        });
    }

    private void recalculateDiscounts(ShoppingCart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cart.getPromoCode() == null || cart.getPromoCode().isBlank()) {
            cart.setDiscountAmount(BigDecimal.ZERO);
            return;
        }

        Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndIsActiveTrue(cart.getPromoCode());
        if (promoOpt.isEmpty() || (promoOpt.get().getMinOrderAmount() != null && subtotal.compareTo(promoOpt.get().getMinOrderAmount()) < 0)) {
            cart.setDiscountAmount(BigDecimal.ZERO);
            return;
        }

        PromoCode promo = promoOpt.get();
        BigDecimal discount;
        if ("PERCENTAGE".equalsIgnoreCase(promo.getDiscountType())) {
            discount = subtotal.multiply(promo.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (promo.getMaxDiscountAmount() != null && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                discount = promo.getMaxDiscountAmount();
            }
        } else {
            discount = promo.getDiscountValue();
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        cart.setDiscountAmount(discount);
    }

    public CartResponse mapToResponse(ShoppingCart cart) {
        List<CartItemDto> itemDtos = cart.getItems() != null
                ? cart.getItems().stream()
                        .map(i -> CartItemDto.builder()
                                .id(i.getId())
                                .productId(i.getProductId())
                                .variantId(i.getVariantId())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .totalPrice(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                                .currency(i.getCurrency())
                                .productName(i.getProductName())
                                .productImage(i.getProductImage())
                                .color(i.getColor())
                                .size(i.getSize())
                                .sku(i.getSku())
                                .build())
                        .toList()
                : List.of();

        int totalQty = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();
        BigDecimal subtotal = itemDtos.stream().map(CartItemDto::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .guestSessionToken(cart.getGuestSessionToken())
                .items(itemDtos)
                .totalQuantity(totalQty)
                .subtotal(subtotal)
                .promoCode(cart.getPromoCode())
                .discountAmount(discount)
                .total(total)
                .currency("USD")
                .build();
    }
}
