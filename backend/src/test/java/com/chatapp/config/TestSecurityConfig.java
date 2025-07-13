package com.chatapp.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig implements WebMvcConfigurer {

  @Bean
  @Primary
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/rooms/**")
                    .permitAll()
                    .requestMatchers("/api/messages/**")
                    .permitAll()
                    .requestMatchers("/api/users/**")
                    .permitAll()
                    .requestMatchers("/api/files/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .oauth2Login(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  @Primary
  public SocketIOServer testSocketIOServer() {
    return mock(SocketIOServer.class);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new PageableHandlerMethodArgumentResolver());
    resolvers.add(new SortHandlerMethodArgumentResolver());
  }
}