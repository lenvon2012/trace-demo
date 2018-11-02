package com.trace;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.SpanInjector;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.trace.demo.util.CustomHttpServletResponseSpanInjector;
import com.trace.demo.util.HttpResponseInjectingTraceFilter;


@SpringBootApplication(scanBasePackages = { "com.trace.demo" })
public class DemoWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoWebApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public SpanInjector<HttpServletResponse> customHttpServletResponseSpanInjector() {
		return new CustomHttpServletResponseSpanInjector();
	}

	@Bean
	public HttpResponseInjectingTraceFilter responseInjectingTraceFilter(Tracer tracer) {
		return new HttpResponseInjectingTraceFilter(tracer, customHttpServletResponseSpanInjector());
	}
	
    @Bean
    public Sampler defaultSampler() {
        return new AlwaysSampler();
    }

}
