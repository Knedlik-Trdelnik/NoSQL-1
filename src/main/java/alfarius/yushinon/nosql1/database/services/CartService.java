package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.entity.redis.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, CartItem> cartRedisTemplate;

    private static final Duration CART_TTL =
            Duration.ofMinutes(30);

    public void saveCart(Long userId, CartItem item) {

        String key = "cart:" + userId;

        cartRedisTemplate.opsForValue().set(
                key,
                item,
                CART_TTL
        );
    }

    public CartItem getCart(Long userId) {

        return cartRedisTemplate
                .opsForValue()
                .get("cart:" + userId);
    }

    public void deleteCart(Long userId) {

        cartRedisTemplate.delete("cart:" + userId);
    }
}