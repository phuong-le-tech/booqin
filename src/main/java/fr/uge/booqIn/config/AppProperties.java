package fr.uge.booqIn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Map<String, String> clientUrls = new HashMap<>();

    private String defaultUrl;

    public Map<String, String> getClientUrls() {
        return clientUrls;
    }

    public void setClientUrls(Map<String, String> clientUrls) {
        this.clientUrls = clientUrls;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }
}
