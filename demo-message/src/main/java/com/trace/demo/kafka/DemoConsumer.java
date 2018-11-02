package com.trace.demo.kafka;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.demo.model.KafkaModel;

@EnableBinding(Sink.class)
public class DemoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DemoConsumer.class.getName());

    @SuppressWarnings("unused")
    @Autowired
    private Tracer tracer;

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("rawtypes")
//    @ServiceActivator(inputChannel = Sink.INPUT)
    @StreamListener(Sink.INPUT)
    public void recieveMessage(GenericMessage message) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        logger.info("Got some message from kafka topic :");
        KafkaModel kafkaModel = jsonMapper.readValue((String) message.getPayload(), KafkaModel.class);
        logger.info("Recieved Message from Kafka topic ::: topic : ");
        logger.info("Id ::" + kafkaModel.getId() + "   Name :: " + kafkaModel.getName());
        Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("msg", message.getPayload());
        this.restTemplate.getForObject("http://localhost:9000/api?msg={msg}", String.class, uriVariables);
    }
}
