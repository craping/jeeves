package com.cherry.jeeves;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application.properties")
@ComponentScan("com.cherry.jeeves")
public class JeevesApplication {

    public static ApplicationContext context;
    
    public static void main(String[] args) {
    	context = new AnnotationConfigApplicationContext(JeevesApplication.class);
    	context.getBean(Jeeves.class).start();
    }
}
