package com.trace.demo.service.redis.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

public class RedisCustomerInfoPublisher implements CustomerInfoPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RedisCustomerInfoPublisher.class.getName());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.docs.docsdemo.service.redis.pubsub.CustomerInfoPublisher#publish()
     */
    /**
     * @param redisTemplate
     * @param topic
     */
    public RedisCustomerInfoPublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        super();
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(Object message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
        logger.info("Putting message into redis topic name : {} with message {}", topic.getTopic(), message);
    }

}
