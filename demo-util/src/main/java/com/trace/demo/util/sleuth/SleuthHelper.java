
package com.trace.demo.util.sleuth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.messaging.TraceMessageHeaders;

/**
 * @Desc
 * 
 * 
 */
public class SleuthHelper {
    public static void continueSpan(Tracer tracer, Map<String, String> map) {
        if (tracer != null && map != null && !map.isEmpty()) {
            tracer.continueSpan(fromMap(map));
        }
    }

    public static void joinSpan(Tracer tracer, Map<String, String> map) {
        if (tracer != null && map != null && !map.isEmpty()) {
            Span parent = fromMap(map);
            if (parent != null) {
                tracer.createSpan(parent.getName(), parent);
            }
        }
    }

    public static Map<String, String> toMap(Span span) {
        if (span == null) {
            return Collections.emptyMap();
        } else {
            return convertSpanToMap(span);
        }
    }

    private static Map<String, String> convertSpanToMap(Span span) {
        Map<String, String> map = new HashMap<>();

        List<String> parents = span.getParents().stream().map(String::valueOf).collect(Collectors.toList());

        map.put(TraceMessageHeaders.SPAN_NAME_NAME, span.getName());
        map.put(TraceMessageHeaders.PARENT_ID_NAME, String.join(",", parents));
        map.put(TraceMessageHeaders.TRACE_ID_NAME, String.valueOf(span.getTraceId()));
        map.put(TraceMessageHeaders.PROCESS_ID_NAME, span.getProcessId());
        map.put(TraceMessageHeaders.SPAN_ID_NAME, String.valueOf(span.getSpanId()));

        span.getBaggage().forEach((key, value) -> map.put(Span.SPAN_BAGGAGE_HEADER_PREFIX + key, value));

        return map;
    }

    public static Span fromMap(Map<String, String> map) {
        return Span.builder().name(map.get(TraceMessageHeaders.SPAN_NAME_NAME))
            .parents(getParents(map.get(TraceMessageHeaders.PARENT_ID_NAME)))
            .traceId(Long.valueOf(map.get(TraceMessageHeaders.TRACE_ID_NAME)))
            .processId(map.get(TraceMessageHeaders.PROCESS_ID_NAME))
            .spanId(Long.valueOf(map.get(TraceMessageHeaders.SPAN_ID_NAME))).baggage(getBaggage(map)).build();
    }

    private static Map<String, String> getBaggage(Map<String, String> map) {
        List<Entry<String, String>> collect = map.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(Span.SPAN_BAGGAGE_HEADER_PREFIX)).collect(Collectors.toList());

        Map<String, String> baggage = new HashMap<>(collect.size());
        collect.forEach(entry -> {
            String key = entry.getKey().replaceFirst(Span.SPAN_BAGGAGE_HEADER_PREFIX, "");
            baggage.put(key, (String) entry.getValue());
        });
        return baggage;
    }

    private static List<Long> getParents(String parents) {
        if (parents == null || parents.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(parents.split(",")).map(Long::parseLong).collect(Collectors.toList());
        }
    }
}
