/**
 * 
 */
package com.trace.demo.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanInjector;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author linfeng_li
 *
 */
public class HttpResponseInjectingTraceFilter extends GenericFilterBean {

    private final Tracer tracer;
    private final SpanInjector<HttpServletResponse> spanInjector;

    /**
     * 
     */
    public HttpResponseInjectingTraceFilter(Tracer tracer, SpanInjector<HttpServletResponse> spanInjector) {
        this.tracer = tracer;
        this.spanInjector = spanInjector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Span currentSpan = this.tracer.getCurrentSpan();
        this.spanInjector.inject(currentSpan, response);
        filterChain.doFilter(request, response);
    }

}
