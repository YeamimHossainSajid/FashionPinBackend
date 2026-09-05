package com.fashionpin.shoppingservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.shoppingservice.dto.AddToCartRequest;
import com.fashionpin.shoppingservice.dto.ApplyPromoRequest;
import com.fashionpin.shoppingservice.dto.CartResponse;
import com.fashionpin.shoppingservice.dto.MergeCartRequest;
import com.fashionpin.shoppingservice.dto.UpdateCartItemRequest;
import com.fashionpin.shoppingservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart and Promo Code endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping({"/api/cart", "/api/v1/cart"})
    @Operation(summary = "Get shopping cart (Supports Guest Session Token header or Authenticated JWT)")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.getCart(userId, guestToken);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping({"/api/cart/items", "/api/v1/cart/items"})
    @Operation(summary = "Add item / variant to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken,
            @Valid @RequestBody AddToCartRequest request) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.addToCart(userId, guestToken, request);
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", response));
    }

    @PatchMapping({"/api/cart/items/{itemId}", "/api/v1/cart/items/{itemId}"})
    @Operation(summary = "Update item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken,
            @PathVariable("itemId") String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.updateCartItem(userId, guestToken, itemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", response));
    }

    @DeleteMapping({"/api/cart/items/{itemId}", "/api/v1/cart/items/{itemId}"})
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken,
            @PathVariable("itemId") String itemId) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.removeCartItem(userId, guestToken, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Cart item removed", response));
    }

    @PostMapping({"/api/cart/apply-promo", "/api/v1/cart/apply-promo"})
    @Operation(summary = "Apply promo code to cart")
    public ResponseEntity<ApiResponse<CartResponse>> applyPromo(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken,
            @Valid @RequestBody ApplyPromoRequest request) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.applyPromoCode(userId, guestToken, request.getPromoCode());
        return ResponseEntity.ok(ApiResponse.ok("Promo code applied successfully", response));
    }

    @DeleteMapping({"/api/cart/promo", "/api/v1/cart/promo"})
    @Operation(summary = "Remove promo code from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removePromo(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.removePromoCode(userId, guestToken);
        return ResponseEntity.ok(ApiResponse.ok("Promo code removed", response));
    }

    @PostMapping({"/api/cart/merge", "/api/v1/cart/merge"})
    @Operation(summary = "Merge guest cart into authenticated account upon login")
    public ResponseEntity<ApiResponse<CartResponse>> mergeCart(
            Principal principal,
            @Valid @RequestBody MergeCartRequest request) {
        String userId = principal != null ? principal.getName() : null;
        CartResponse response = cartService.mergeCart(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cart merged successfully", response));
    }

    @DeleteMapping({"/api/cart/internal/{cartId}", "/api/v1/cart/internal/{cartId}"})
    @Operation(summary = "Internal: clear cart after successful order placement")
    public ResponseEntity<ApiResponse<Void>> clearCartInternal(@PathVariable("cartId") String cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", null));
    }
}
