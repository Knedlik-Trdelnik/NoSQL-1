package alfarius.yushinon.nosql1.config;

import alfarius.yushinon.nosql1.entity.redis.CartItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;


import org.springframework.data.redis.serializer.*;


@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, CartItem> cartRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, CartItem> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<CartItem> valueSerializer =
                new JacksonJsonRedisSerializer<>(CartItem.class);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);

        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }
}