package alfarius.yushinon.nosql1.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackListService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, long expirationMs) {
        if (expirationMs > 0) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + token,
                    "true",
                    expirationMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }
}