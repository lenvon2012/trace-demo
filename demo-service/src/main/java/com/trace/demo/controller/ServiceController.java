package com.trace.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.demo.model.KafkaModel;
import com.trace.demo.service.redis.pubsub.CustomerInfoPublisher;

@RestController
@EnableBinding(Source.class)
@RequestMapping("/demo/api")
public class ServiceController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class.getName());

    @Autowired
    private Source source;

    @Autowired
    private CustomerInfoPublisher customerInfoPublisher;

    @RequestMapping(value = "/")
    public String home() {
        logger.info("Hello Demo Service!");
        return "Hello Demo Service!";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        logger.info("Hello Demo Service Test!");
        return "Hello Demo Service Test!";
    }

    @RequestMapping(value = "/testkafka", method = RequestMethod.GET)
    public String testkafka(@RequestParam(value = "msg") String msg) throws JsonProcessingException {

        logger.info("Hello Demo Service testkafka Test!");

        KafkaModel kafkaModel = new KafkaModel();
        kafkaModel.setId(String.valueOf(Math.round(Math.random() * 99999)));
        kafkaModel.setName(msg);

        ObjectMapper jsonMapper = new ObjectMapper();

        logger.info("Putting message to kafka topic name : sampletopic");

        MessageBuilder<String> messageBuilder = MessageBuilder.withPayload(jsonMapper.writeValueAsString(kafkaModel));

        source.output().send(messageBuilder.build());

        return "Message successfully pushed to kafka";
    }

    @RequestMapping(value = "/testredis", method = RequestMethod.GET)
    public String testredis(@RequestParam(value = "msg") String msg) throws JsonProcessingException {

        logger.info("Hello Demo Service testredis Test!");

        KafkaModel kafkaModel = new KafkaModel();
        kafkaModel.setId(String.valueOf(Math.round(Math.random() * 99999)));
        kafkaModel.setName(msg);

        ObjectMapper jsonMapper = new ObjectMapper();
        
        Object message = jsonMapper.writeValueAsString(kafkaModel);
        
        customerInfoPublisher.publish(message);

        return "Message successfully pushed to redis";
    }
}
