package com.trace.demo.message.redis.pubsub;

import java.io.IOException;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Span.SpanBuilder;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.SleuthProperties;
import org.springframework.cloud.sleuth.instrument.messaging.TraceMessageHeaders;
import org.springframework.cloud.sleuth.log.Slf4jSpanLogger;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.demo.model.redis.RedisDefaultMessage;
import com.trace.demo.model.redis.RedisMessage;
import com.trace.demo.util.sleuth.SleuthHelper;

@Aspect
@Component
public class MessageAspect {

    private static final Logger logger = LoggerFactory.getLogger(MessageAspect.class.getName());

    @Autowired
    private Tracer tracer;

    @Autowired
    private SleuthProperties sleuthProperties;

    @Autowired
    private Slf4jSpanLogger slf4jSpanLogger;

    @Value("${spring.sleuth.supportsJoin}")
    public Boolean supportsJoin;

    @Pointcut("execution(* onMessage(..)) && target(org.springframework.data.redis.connection.MessageListener)")
    public void messageListener() {
        // This func is intentionally empty. Nothing special is needed here.
    }

    @Around("messageListener()")
    public void interceptMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object[] joinPointArgs = joinPoint.getArgs();
            Object[] result = new Object[joinPointArgs.length];
            for (int i = 0; i < joinPointArgs.length; i++) {
                Object object = joinPointArgs[i];
                ObjectMapper objectMapper = new ObjectMapper();
                if (object instanceof Message && jsonParse(object)) {
                    Message message = (Message) object;
                    RedisMessage redisMessage = objectMapper.readValue((String) object.toString(), RedisMessage.class);
                    logger.debug("Received >> redisMessage {} ", redisMessage);
                    Map<String, String> spanMap = redisMessage.getHeader();
                    String traceIdString = Span.idToHex(Long.parseLong(spanMap.get(TraceMessageHeaders.TRACE_ID_NAME)));
                    String spanId = Span.idToHex(Long.parseLong(spanMap.get(TraceMessageHeaders.SPAN_ID_NAME)));
                    Span parentSpan = SleuthHelper.fromMap(spanMap);
                    logger.debug("Received >> trace {} span {}", traceIdString, spanId);
                    if (sleuthProperties != null && sleuthProperties.isSupportsJoin()) {
                        SleuthHelper.joinSpan(tracer, spanMap);
                        logger.debug("Join trace span {}", spanMap);
                    } else {
                        SleuthHelper.continueSpan(tracer, spanMap);
                        logger.debug("Continue trace span {}", spanMap);
                    }
                    slf4jSpanLogger.logStartedSpan(parentSpan, tracer.getCurrentSpan());
                    logger.debug("Received >> trace {} span {}", Span.idToHex(tracer.getCurrentSpan().getTraceId()),
                        Span.idToHex(tracer.getCurrentSpan().getSpanId()));
                    byte[] channel = message.getChannel();
                    byte[] body = SerializationUtils.serialize(redisMessage.getMessage());
                    logger.debug("Received >> message {}", SerializationUtils.deserialize(body));
//                    byte[] body = RedisUtil.objectToByteArray(redisMessage.getMessage());
                    result[i] = new RedisDefaultMessage(channel, body);
                } else {
                    result[i] = object;
                }
            }
            SpanBuilder spanBuilder = Span.builder().from(this.tracer.getCurrentSpan());
            Span currentSpan = spanBuilder.parents(this.tracer.getCurrentSpan().getParents()).name(joinPoint.getSignature().getName()).remote(false).build();
            currentSpan.logEvent("sr");
            this.tracer.close(currentSpan);
            this.tracer.continueSpan(currentSpan);
            joinPoint.proceed(result);
        } catch (JsonParseException e) {
            logger.error("Join trace span with JsonParseException {} ", e);
        } catch (JsonMappingException e) {
            logger.error("Join trace span with JsonMappingException {} ", e);
        } catch (IOException e) {
            logger.error("Join trace span with IOException {}", e);
        }
    }
    
    private boolean jsonParse(Object object) {
        boolean flag;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readValue((String) object.toString(), RedisMessage.class);
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

}
