package com.trace.demo.util;

import javax.servlet.http.HttpServletResponse;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanInjector;

public class CustomHttpServletResponseSpanInjector implements SpanInjector<HttpServletResponse> {

	@Override
	public void inject(Span span, HttpServletResponse carrier) {
		carrier.addHeader(Span.TRACE_ID_NAME, span.traceIdString());
		carrier.addHeader(Span.SPAN_ID_NAME, Span.idToHex(span.getSpanId()));
	}

}
