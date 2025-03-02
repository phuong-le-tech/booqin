package fr.uge.booqIn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements PageableHandlerMethodArgumentResolverCustomizer, WebMvcConfigurer {

  @Override
  public void customize(org.springframework.data.web.PageableHandlerMethodArgumentResolver resolver) {
    resolver.setOneIndexedParameters(false);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // This registers the /static/css/ directory to be served at /css/ URL path
    registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");
  }
}