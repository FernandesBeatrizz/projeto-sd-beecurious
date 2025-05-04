package main.java.meta2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServingWebContentApplication {

    @Bean
    public ServletRegistrationBean<ThymeleafServlet> ServletBean() {
        ServletRegistrationBean<ThymeleafServlet> bean = new ServletRegistrationBean<>(new ThymeleafServlet(), "/thymeleafServlet/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<Example> exampleServletBean() {
        ServletRegistrationBean<Example> bean = new ServletRegistrationBean<>(new Example(), "/exampleServlet/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServingWebContentApplication.class, args);
    }

}