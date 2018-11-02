package com.trace.demo.message.redis.pubsub.config;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import com.trace.demo.message.redis.pubsub.CustomerInfoSubscriber;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ComponentScan({ "com.trace.demo.message.redis" })
public class RedisConfig {

    @Autowired
    private CustomerInfoSubscriber customerInfoSubscriber;

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
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(customerInfoSubscriber);
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(messageListener(), topic());
        container.setTaskExecutor(Executors.newFixedThreadPool(4));
        return container;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic(redisTopic);
    }

}
