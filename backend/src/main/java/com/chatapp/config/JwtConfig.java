package com.chatapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

  private String secret;
  private long expiration = 86400; // 24 hours in seconds

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getExpiration() {
    return expiration;
  }

  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }
}
