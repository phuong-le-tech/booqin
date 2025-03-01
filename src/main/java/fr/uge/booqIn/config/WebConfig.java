package fr.uge.booqIn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements PageableHandlerMethodArgumentResolverCustomizer {

  @Override
  public void customize(org.springframework.data.web.PageableHandlerMethodArgumentResolver resolver) {
    resolver.setOneIndexedParameters(false);
  }
}