package com.trace.demo.message.redis.pubsub;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerInfoSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomerInfoSubscriber.class.getName());
    
    @Autowired
    private RestTemplate restTemplate;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.data.redis.connection.MessageListener#onMessage(org.springframework.data.redis.connection.Message,
     * byte[])
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        logger.info("Received >> message {} , thread {} ", message, Thread.currentThread().getName() );
        Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("msg", message);
        this.restTemplate.getForObject("http://localhost:9000/api?msg={msg}", String.class, uriVariables);
    }

}
