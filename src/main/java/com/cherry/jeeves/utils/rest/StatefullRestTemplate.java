package com.cherry.jeeves.utils.rest;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate with state. Inspired by https://stackoverflow.com/a/12840202/2364882
 */
public class StatefullRestTemplate extends RestTemplate {
    private final HttpContext httpContext;

    StatefullRestTemplate(HttpContext httpContext) {
        super();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.httpContext = httpContext == null ? new BasicHttpContext() : httpContext;
        StatefullHttpComponentsClientHttpRequestFactory statefullHttpComponentsClientHttpRequestFactory
                = new StatefullHttpComponentsClientHttpRequestFactory(httpClient, httpContext);
        super.setRequestFactory(statefullHttpComponentsClientHttpRequestFactory);
        List<HttpMessageConverter<?>> converters = this.getMessageConverters();
        FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
        formConverter.setCharset(Charset.forName("UTF8"));
        converters.add(formConverter);
        
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                List<MediaType> mediaTypes = converter.getSupportedMediaTypes();
                List<MediaType> newMediaTypes = new ArrayList<>(mediaTypes);
                newMediaTypes.add(MediaType.TEXT_HTML);
                ((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(newMediaTypes);
            }
        }
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    class StatefullHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
        private final HttpContext httpContext;

        StatefullHttpComponentsClientHttpRequestFactory(HttpClient httpClient, HttpContext httpContext) {
            super(httpClient);
            this.httpContext = httpContext;
        }

        @Override
        protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
            return this.httpContext;
        }
    }
}