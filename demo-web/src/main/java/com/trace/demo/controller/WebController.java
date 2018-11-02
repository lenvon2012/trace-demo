package com.trace.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/demo/web")
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/")
    public String home() {
        logger.info("Hello Demo Web!");
        return "Hello Demo Web!";
    }

    @RequestMapping("/call")
    public String callHome() {
        logger.info("Calling Home of Demo Service");
        return restTemplate.getForObject("http://localhost:8090/docs/api/", String.class);
    }

    @RequestMapping("/callkafka")
    public String callKafkaTest(@RequestParam(value = "msg") String msg) {
        logger.info("Calling Kafka Test of Demo Service");
        Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("msg", msg);
        return restTemplate.getForObject("http://localhost:8090/demo/api/testkafka?msg={msg}", String.class, uriVariables);
    }

    @RequestMapping("/callredis")
    public String callRedisTest(@RequestParam(value = "msg") String msg) {
        logger.info("Calling Redis Test of Demo Service");
        Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("msg", msg);
        return restTemplate.getForObject("http://localhost:8090/demo/api/testredis?msg={msg}", String.class, uriVariables);
    }
}
