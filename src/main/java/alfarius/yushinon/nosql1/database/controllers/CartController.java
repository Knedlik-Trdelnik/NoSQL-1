package alfarius.yushinon.nosql1.controller;

import alfarius.yushinon.nosql1.database.services.CartService;
import alfarius.yushinon.nosql1.entity.redis.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/{userId}")
    public ResponseEntity<Void> saveCart(
            @PathVariable Long userId,
            @RequestBody CartItem item
    ) {
        cartService.saveCart(userId, item);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartItem> getCart(
            @PathVariable Long userId
    ) {
        CartItem item = cartService.getCart(userId);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteCart(
            @PathVariable Long userId
    ) {
        cartService.deleteCart(userId);

        return ResponseEntity.noContent().build();
    }
}