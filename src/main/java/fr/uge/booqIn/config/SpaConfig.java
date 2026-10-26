package fr.uge.booqIn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Profil demo : sert le build React (classpath:/static) et renvoie index.html
 * pour les routes du front (react-router) qui ne correspondent a aucun fichier.
 */
@Configuration
@Profile("demo")
public class SpaConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
              @Override
              protected Resource getResource(String path, Resource location) throws java.io.IOException {
                if (path.startsWith("api/")) {
                  return null;
                }
                Resource requested = location.createRelative(path);
                return requested.exists() && requested.isReadable()
                        ? requested
                        : new ClassPathResource("/static/index.html");
              }
            });
  }
}
