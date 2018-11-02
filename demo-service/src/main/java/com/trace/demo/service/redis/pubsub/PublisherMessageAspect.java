package com.trace.demo.service.redis.pubsub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.ErrorParser;
import org.springframework.cloud.sleuth.Log;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Span.SpanBuilder;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.messaging.TraceMessageHeaders;
import org.springframework.cloud.sleuth.sampler.NeverSampler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.demo.model.redis.RedisMessage;
import com.trace.demo.util.sleuth.SleuthHelper;

@Aspect
@Component
public class PublisherMessageAspect {

    private static final Logger logger = LoggerFactory.getLogger(PublisherMessageAspect.class.getName());

    @Autowired
    private Tracer tracer;

    @Autowired
    private ErrorParser errorParser;

    @Pointcut("execution(* publish(..)) && target(com.trace.demo.service.redis.pubsub.CustomerInfoPublisher)")
    public void messageListener() {
        // This func is intentionally empty. Nothing special is needed here.
    }

    public void beforeSentMessage(JoinPoint joinPoint) throws Throwable {
        Object[] joinPointArgs = joinPoint.getArgs();
        Map<String, Object> messageObj = new HashMap<>();
        Map<String, String> spanObj = SleuthHelper.toMap(tracer.getCurrentSpan());
        messageObj.put("contentType", "application/json");
        messageObj.put("header", spanObj);
        RedisMessage redisMessage = new RedisMessage("application/json", spanObj, joinPointArgs[0]);
        Span span = startSpan(this.tracer.getCurrentSpan(), joinPoint.getSignature().getName(), redisMessage);
        SpanBuilder spanBuilder = Span.builder().from(span);
        Span currentSpan = spanBuilder.remote(false).build();
        this.tracer.continueSpan(currentSpan);
    }

    @Around("messageListener()")
    public void interceptMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] joinPointArgs = joinPoint.getArgs();
        Map<String, Object> messageObj = new HashMap<>();
        createSpanIntoTrace(joinPoint);
        Map<String, String> spanObj = SleuthHelper.toMap(tracer.getCurrentSpan());
        messageObj.put("contentType", "application/json");
        messageObj.put("header", spanObj);
        RedisMessage redisMessage = new RedisMessage("application/json", spanObj, joinPointArgs[0]);
        ObjectMapper jsonMapper = new ObjectMapper();
        Object mesgObj = jsonMapper.writeValueAsString(redisMessage);
        Object[] message = new Object[] { mesgObj };
        joinPoint.proceed(message);
        logger.info("Putting message into redis with message {}", message);
        afterSendCompletion(mesgObj, joinPoint.getSignature().getName(), null);
    }

    /**
     * @param joinPoint
     */
    private void createSpanIntoTrace(ProceedingJoinPoint joinPoint) {
        Span span = startSpan(this.tracer.getCurrentSpan(), joinPoint.getSignature().getName());
        SpanBuilder spanBuilder = Span.builder().from(span);
        Span currentSpan = spanBuilder.remote(false).build();
        this.tracer.continueSpan(currentSpan);
    }

    public void afterSendCompletion(Object object, String name, Exception ex) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RedisMessage redisMessage = objectMapper.readValue((String) object.toString(), RedisMessage.class);
            Map<String, String> spanMap = redisMessage.getHeader();
            Span currentSpan = this.tracer.isTracing() ? this.tracer.getCurrentSpan() : SleuthHelper.fromMap(spanMap);
            if (logger.isDebugEnabled()) {
                logger.debug("Completed sending and current span is " + currentSpan);
            }
            if (containsServerReceived(currentSpan)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Marking span with server send");
                }
                currentSpan.logEvent(Span.SERVER_SEND);
            } else if (currentSpan != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Marking span with client received");
                }
                currentSpan.logEvent(Span.CLIENT_RECV);
            }
            addErrorTag(ex);
            if (logger.isDebugEnabled()) {
                logger.debug("Closing messaging span " + currentSpan);
            }
            this.tracer.close(currentSpan);
            if (logger.isDebugEnabled()) {
                logger.debug("Messaging span " + currentSpan + " successfully closed");
            }
        } catch (IOException e) {
            logger.error("Parse Messaging span {} with exception {}", this.tracer.getCurrentSpan(), e);
        }
    }

    private boolean containsServerReceived(Span span) {
        if (span == null) {
            return false;
        }
        for (Log log : span.logs()) {
            if (Span.SERVER_RECV.equals(log.getEvent())) {
                return true;
            }
        }
        return false;
    }

    private void addErrorTag(Exception ex) {
        if (ex != null) {
            this.errorParser.parseErrorTags(this.tracer.getCurrentSpan(), ex);
        }
    }

    private Span startSpan(Span span, String name) {
        return startSpan(span, name, null);
    }

    private Span startSpan(Span span, String name, RedisMessage message) {
        if (span != null) {
            return this.tracer.createSpan(name, span);
        }
        if (message != null && Span.SPAN_NOT_SAMPLED.equals(message.getHeader().get(TraceMessageHeaders.SAMPLED_NAME))) {
            return this.tracer.createSpan(name, NeverSampler.INSTANCE);
        }
        return this.tracer.createSpan(name);
    }

}
