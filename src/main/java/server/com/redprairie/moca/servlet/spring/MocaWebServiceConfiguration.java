/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.servlet.spring;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.jackson.SimpleMocaResultsSerializer;
import com.redprairie.moca.servlet.jackson.handlers.UnknownPropertyProblemHandler;
import com.redprairie.moca.servlet.spring.marshallers.HibernateMapperXStreamMarshaller;
import com.redprairie.moca.servlet.spring.views.XmlMarshallingView;
import com.redprairie.moca.servlet.xstream.converters.HibernatePersistentCollectionConverter;
import com.redprairie.moca.servlet.xstream.converters.HibernatePersistentMapConverter;
import com.redprairie.moca.servlet.xstream.converters.HibernatePersistentSortedMapConverter;
import com.redprairie.moca.servlet.xstream.converters.HibernatePersistentSortedSetConverter;
import com.redprairie.moca.servlet.xstream.converters.UnmodifiableMapConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * This is the MOCA Spring Mvc based configuration file.  This controls
 * setting up default values from the application context it is stored in.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Configuration
public class MocaWebServiceConfiguration extends WebMvcConfigurationSupport {
    
    // @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        Map<String, HandlerInterceptor> interceptors = 
                _applicationContext.getBeansOfType(HandlerInterceptor.class);
        for (HandlerInterceptor interceptor : interceptors.values()) { 
            registry.addInterceptor(interceptor);
            _logger.debug("Added interceptor: {}", interceptor);
        }
        
        _logger.debug("Configured interceptors: {}", interceptors);
    }
    
    // @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#addArgumentResolvers(java.util.List)
    @Override
    protected void addArgumentResolvers(
        List<HandlerMethodArgumentResolver> argumentResolvers) {
        Map<String, HandlerMethodArgumentResolver> resolvers = 
                _applicationContext.getBeansOfType(HandlerMethodArgumentResolver.class);
        for (HandlerMethodArgumentResolver resolver : resolvers.values()) {
            argumentResolvers.add(resolver);
            _logger.debug("Added method argument resolver: {}", resolver);
        }
        
        _logger.debug("Configured method argument resolvers: {}", argumentResolvers);
    }
    
    @Override
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        Map<String, HandlerMethodReturnValueHandler> handlers = _applicationContext
            .getBeansOfType(HandlerMethodReturnValueHandler.class);
        for (HandlerMethodReturnValueHandler handler : handlers.values()) {
            returnValueHandlers.add(handler);
            _logger.debug("Added method return value handler: {}", handler);
        }
        
        _logger.debug("Configured method return value handlers: {}", returnValueHandlers);
    }
    
    // @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureHandlerExceptionResolvers(java.util.List)
    @Override
    public void configureHandlerExceptionResolvers(
            List<HandlerExceptionResolver> exceptionResolvers) {
        
        // Precedence is from first to last but we want the Spring default
        // handlers to run first so annotation based exception handling takes top precedence
        addDefaultHandlerExceptionResolvers(exceptionResolvers);
        
        // Classpath scan for the additional custom handlers
        Map<String, HandlerExceptionResolver> resolvers = 
                _applicationContext.getBeansOfType(HandlerExceptionResolver.class);
        for (HandlerExceptionResolver resolver : resolvers.values()) {
            exceptionResolvers.add(resolver);
            _logger.debug("Added exception resolver: {}", resolver);
        }

        _logger.debug("Configured exception resolver handlers: {}", exceptionResolvers);
    }
    
    @SuppressWarnings("deprecation")
    @Bean
    public ContentNegotiatingViewResolver contentNegotiatingViewResolver() {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        // Make the content type resolver right in the middle, all the other
        // type resolvers will be lower, but custom ones should be higher
        resolver.setOrder(0);
        
        List<View> views = new ArrayList<View>();
        Map<String, View> viewMap = 
                _applicationContext.getBeansOfType(View.class);
        for (View view : viewMap.values()) {
            views.add(view);
        }
        
        MappingJacksonJsonView jacksonView = new MappingJacksonJsonView();
        jacksonView.setObjectMapper(objectMapper());
        views.add(jacksonView);
        
        XmlMarshallingView xmlView = new XmlMarshallingView(xStreamMarshaller());
        views.add(xmlView);
        
        resolver.setDefaultViews(views);
        resolver.setDefaultContentType(MediaType.APPLICATION_JSON);
        return resolver;
    }
    
    /**
     * Expose the ObjectMapper to others so they can add additional configuration
     * parameters over and above ours
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        configureObjectMapper(mapper);
        
        return mapper;
    }
    
    @Bean
    public XStreamMarshaller xStreamMarshaller() {
        XStreamMarshaller marshaller = new HibernateMapperXStreamMarshaller();
        XStream xstream = marshaller.getXStream();
        Mapper mapper = xstream.getMapper();
        xstream.registerConverter(
            new HibernateProxyConverter()
            , XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(
            new HibernatePersistentCollectionConverter(mapper)
            , XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(
            new HibernatePersistentSortedMapConverter(mapper)
            , XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(
            new HibernatePersistentSortedSetConverter(mapper)
            , XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(
            new HibernatePersistentMapConverter(mapper)
            , XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(new UnmodifiableMapConverter(mapper), 
            XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(
            new JavaBeanConverter(mapper)
            , XStream.PRIORITY_VERY_LOW);
        xstream.registerConverter(new DateConverter(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            null, TimeZone.getTimeZone("UTC")), XStream.PRIORITY_VERY_HIGH);
        xstream.autodetectAnnotations(true);
        xstream.setMode(XStream.NO_REFERENCES);
        return marshaller;
    }
    
    private static void configureObjectMapper(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("MocaResultsModule", 
            new Version(1, 0, 0, null));
        module.addSerializer(MocaResults.class, new SimpleMocaResultsSerializer());
        mapper.registerModule(module);
        mapper.getDeserializationConfig().addHandler(new UnknownPropertyProblemHandler());
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(dateFormat);
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        @SuppressWarnings("rawtypes")
        Map<String, HttpMessageConverter> messageConverters = 
                _applicationContext.getBeansOfType(HttpMessageConverter.class);
        for (HttpMessageConverter<?> converter : messageConverters.values()) {
            converters.add(converter);
            _logger.debug("Added HTTP Message Converter: {}", converter);
        }

        // Add all the defaults after we loaded our customized ones
        converters.addAll(getDefaultHttpMessageConverters());
        
        for (HttpMessageConverter<?> converter : converters) {
            // We don't want to check instanceof since we extend it
            if (converter.getClass() == MappingJacksonHttpMessageConverter.class) {
                ((MappingJacksonHttpMessageConverter)converter).setObjectMapper(
                    objectMapper());
            }
        }
        
        _logger.debug("Configured message converters: {}", converters);
    }

    // @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        _applicationContext = applicationContext;
        super.setApplicationContext(applicationContext);
    }
    
    // This method is just used to get the default http message handlers from
    // Spring but for now we want to explicitly replace the Jackson 2 mapper
    // with Jackson 1 even if Jackson 2 is present in the environment.
    private List<HttpMessageConverter<?>> getDefaultHttpMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        addDefaultHttpMessageConverters(converters);
        ListIterator<HttpMessageConverter<?>> it = converters.listIterator();
        // TODO: When we upgrade to Jackson 2 we don't need to replace the Jackson 2 converter anymore
        while (it.hasNext()) {
            HttpMessageConverter<?> converter = it.next();
            if (converter.getClass() == MappingJackson2HttpMessageConverter.class) {
                _logger.debug("Replacing the Jackson 2 http message converter with Jackson 1");
                it.set(new MappingJacksonHttpMessageConverter());
            }
        }
        
        return converters;
    }
    
    private ApplicationContext _applicationContext;
    
    private static final Logger _logger = LogManager.getLogger(
        MocaWebServiceConfiguration.class);
}
