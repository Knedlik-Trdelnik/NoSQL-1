package alfarius.yushinon.nosql1.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        String key = "rate:limit:" + ip;

        // Значит так меченный, не больше 10 палок докторской колбасы на рыло, ты меня понял ?
        Long requests = redisTemplate.opsForValue().increment(key);
        if (requests == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        if (requests > 10) {
            response.setStatus(429); // я же тебе блять русским языком сказал, что не больше 10 палок, видишь что блять случилось ? На тебя таможенники бесятся, не пускают тебя к нам больше
            response.getWriter().write("Неа, хватит с тебя на сегодня");
            return false;
        }

        return true;
    }
}