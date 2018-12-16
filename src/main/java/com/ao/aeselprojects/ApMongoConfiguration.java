/*
Apache2 License Notice
Copyright 2018 Alex Barry
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.ao.aeselprojects;

import com.ao.aeselprojects.auth.ApBasicAuthEntryPoint;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

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
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpMethod;

@Configuration
public class ApMongoConfiguration extends AbstractMongoConfiguration {

  // Hostname of Mongo Connection
  @Value("${mongo.hosts:localhost}")
  private String mongoHosts;

  // Port of Mongo Connection
  @Value("${mongo.port:27017}")
  private int mongoPort;

  // Is Authentication Active in the Mongo Connection
  @Value("${mongo.auth.active:false}")
  private boolean mongoAuthActive;

  // Username of the Mongo Connection
  @Value("${mongo.auth.username:mongo}")
  private String mongoUsername;

  // Password of the Mongo Connection
  @Value("${mongo.auth.password:mongo}")
  private String mongoPassword;

  // Is Mongo SSL Connectivity Enabled
  @Value("${mongo.ssl.enabled:false}")
  private boolean mongoSslEnabled;

  // Trust Store Path
  @Value("${ssl.trustStore.path:}")
  private String mongoTrustStore;

  // Trust Store Password
  @Value("${ssl.trustStore.password:}")
  private String mongoTrustStorePw;

  // Keystore Path
  @Value("${ssl.keyStore.path:}")
  private String mongoKeyStore;

  // Keystore Password
  @Value("${ssl.keyStore.password:}")
  private String mongoKeyStorePw;

  // -------- Mongo Configuration -----------

  // Connect to Mongo, potentially authenticated
  @Override
  @Bean
  public MongoClient mongoClient() {
    // Build Mongo Connection Ops

    // Build out the Mongo SSL Options
    MongoClientOptions options;
    MongoClientOptions.Builder builder = MongoClientOptions.builder();
    if (mongoSslEnabled) {
      if (!mongoTrustStore.isEmpty()) {
        System.setProperty("javax.net.ssl.trustStore", mongoTrustStore);
      }
      if (!mongoTrustStorePw.isEmpty()) {
        System.setProperty("javax.net.ssl.trustStorePassword", mongoTrustStorePw);
      }
      if (!mongoKeyStore.isEmpty()) {
        System.setProperty("javax.net.ssl.keyStore", mongoKeyStore);
      }
      if (!mongoKeyStorePw.isEmpty()) {
        System.setProperty("javax.net.ssl.keyStorePassword", mongoKeyStorePw);
      }
      options = builder.sslEnabled(true).build();
    } else {
      options = builder.build();
    }

    // Setup the list of Mongo Addresses
    List<ServerAddress> mongoAdressList = new ArrayList<ServerAddress>();
    String[] addressArray = mongoHosts.split(",");
    for (String address : addressArray) {
      mongoAdressList.add(new ServerAddress(address, mongoPort));
    }

    // Pull authentication information
    if (mongoAuthActive) {
      List<MongoCredential> mongoCredsList = new ArrayList<MongoCredential>();
      mongoCredsList.add(MongoCredential.createCredential(
          mongoUsername,
          "_projects",
          mongoPassword.toCharArray()));

      // Return a DB Client with Authentication
      return new MongoClient(mongoAdressList, mongoCredsList, options);
    }

    // Return a DB Client without Authentication
    return new MongoClient(mongoAdressList, options);
  }

  // Define Mongo Database name
  @Override
  protected String getDatabaseName() {
    return "_projects";
  }
}
