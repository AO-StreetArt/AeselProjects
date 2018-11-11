package com.ao.aeselprojects;

import com.ao.aeselprojects.ApMongoConfiguration;

import com.ao.aeselprojects.auth.ApBasicAuthEntryPoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableRetry
@EnableWebSecurity
@Import(ApMongoConfiguration.class)
@SpringBootApplication(exclude = {SolrAutoConfiguration.class})
public class AeselprojectsApplication extends WebSecurityConfigurerAdapter {

  // Is Authentication required for accessing our HTTP Server
  @Value("${server.auth.active:false}")
  private boolean httpAuthActive;

  // Username for the HTTP Server Authentication
  @Value("${server.auth.username:admin}")
  private String httpUsername;

  // Password for the HTTP Server Authentication
  @Value("${server.auth.password:admin}")
  private String httpPassword;

  // -------- Security Configuration ---------

  // Security Realm
  private static String REALM = "PROJECTS_REALM";

  BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();


  // Configure Basic Auth
  @Autowired
  public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
    if (httpAuthActive) {
      //User.withDefaultPasswordEncoder().username("user").password("user").roles("USER").build();
      auth.inMemoryAuthentication().passwordEncoder(passEncoder)
          .withUser(httpUsername).password(passEncoder.encode(httpPassword)).roles("USER");
    }
  }

  // Set up security filters
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (httpAuthActive) {
      http.csrf().disable()
          .authorizeRequests().anyRequest().authenticated()
          .and().httpBasic().realmName(REALM).authenticationEntryPoint(getBasicAuthEntryPoint())
          .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    } else {
      // Security Disabled
      http.csrf().disable()
          .authorizeRequests().anyRequest().permitAll();
    }
  }

  // Set entrypoint for Authentication Failures
  @Bean
  public ApBasicAuthEntryPoint getBasicAuthEntryPoint() {
    return new ApBasicAuthEntryPoint();
  }

  // -------- Main App ---------

  public static void main(String[] args) {
    SpringApplication.run(AeselprojectsApplication.class, args);
  }
}
