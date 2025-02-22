package fr.uge.booqIn.singleton;

import org.springframework.web.reactive.function.client.WebClient;

public class WebClientSingleton {
  private WebClientSingleton(){

  }

  private static final WebClient WEB_CLIENT_INSTANCE = WebClient.create();

  public static WebClient getInstance(){
    return WEB_CLIENT_INSTANCE;
  }

}
