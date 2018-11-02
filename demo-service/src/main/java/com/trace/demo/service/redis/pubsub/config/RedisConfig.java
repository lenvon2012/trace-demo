package com.trace.demo.service.redis.pubsub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import com.trace.demo.service.redis.pubsub.CustomerInfoPublisher;
import com.trace.demo.service.redis.pubsub.RedisCustomerInfoPublisher;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ComponentScan({ "com.trace.demo.service" })
public class RedisConfig {
    @Value("${spring.redis.topic}")
    private String redisTopic;

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPwd;

    @Value("${spring.redis.timeout}")
    private Integer redisTimeOut;

    @Value("${spring.redis.pool.min-evictable-idle}")
    private Integer minEvictableIdleTimeMillis;

    /**
     * jedisConnectionFactory
     * 
     * @return JedisConnectionFactory
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(redisHost);
        factory.setPort(redisPort);
        factory.setPassword(redisPwd);
        factory.setUsePool(true);
        factory.setTimeout(redisTimeOut);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        factory.setPoolConfig(poolConfig);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        return template;
    }

    @Bean
    CustomerInfoPublisher redisPublisher() {
        return new RedisCustomerInfoPublisher(redisTemplate(), topic());
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic(redisTopic);
    }

}
