package alfarius.yushinon.nosql1.config;

import alfarius.yushinon.nosql1.entity.redis.CartItem;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;


import org.springframework.data.redis.serializer.*;

import java.time.Duration;


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

    @Value("${spring.data.redis.password:#{madoka_winwin}}")
    private String redisPassword;

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        var singleServer = config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort);

        // Если пароль задан — указываем его
        if (redisPassword != null && !redisPassword.isBlank()) {
            singleServer.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL = 10 minutes
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}