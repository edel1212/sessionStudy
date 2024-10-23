package com.yoo.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession  // Redis 세션 사용을 활성화
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());  // 키를 문자열로 직렬화
        redisTemplate.setValueSerializer(new StringRedisSerializer());  // 값도 문자열로 직렬화
        return redisTemplate;
    }

    @Bean
    public RedisIndexedSessionRepository redisIndexedSessionRepository(RedisTemplate<String, Object> redisTemplate) {
        return new RedisIndexedSessionRepository(redisTemplate);
    }
}