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

package com.ao.aeselprojects.controller;

import com.ao.aeselprojects.dao.ProjectRepository;
import com.ao.aeselprojects.model.Project;
import com.ao.aeselprojects.model.SceneGroup;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.bson.Document;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
* Rest Controller defining the Project API.
* Responsible for handling and responding to all Project API Requests.
*/
@Controller
public class SceneGroupController {

  // Spring Data Mongo Repository allowing access to standard Mongo operations
  @Autowired
  ProjectRepository projects;

  // Asset Controller Logger
  private static final Logger logger =
      LogManager.getLogger("aeselprojects.SceneGroupController");

  @Autowired
  MongoDatabase mongoDb;
  MongoCollection<Document> mongoCollection = null;
  private String mongoCollectionName = "projects";

  /**
  * Use the Mongo Client to access the database and collection.
  */
  @PostConstruct
  public void init() {
    mongoCollection = mongoDb.getCollection(mongoCollectionName);
  }

  private ResponseEntity<String> genHttpResponseFromUpdate(UpdateResult result) {
    // Construct and send the HTTP response based on Mongo result
    HttpStatus returnCode = HttpStatus.OK;
    if (result.getModifiedCount() < 1) {
      returnCode = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
      logger.debug("No documents modified for Scene Group addition");
    }
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", "application/json");
    return new ResponseEntity<String>("", responseHeaders, returnCode);
  }

  /**
  * Create a new Scene Group in an existing Project.
  */
  @PostMapping("/v1/project/{projectKey}/groups")
  @ResponseBody
  public ResponseEntity<String> createSceneGroup(
      @PathVariable String projectKey,
      @RequestBody SceneGroup inpGroup,
      @RequestHeader(name="X-Aesel-Principal", defaultValue="") String aeselPrincipal) {
    logger.info("Attempting to add Scene Group {} to Project {} with user {}", inpGroup.getName(), projectKey, aeselPrincipal);

    // Build the Mongo Update Queries
    BasicDBObject sgObject = new BasicDBObject();
    sgObject.put("name", inpGroup.getName());
    sgObject.put("description", inpGroup.getDescription());
    sgObject.put("category", inpGroup.getCategory());
    if (inpGroup.getScenes() == null) {
      sgObject.put("scenes", new ArrayList<String>());
    } else {
      sgObject.put("scenes", inpGroup.getScenes());
    }
    BasicDBObject innerUpdateQuery = new BasicDBObject();
    innerUpdateQuery.put("sceneGroups", sgObject);
    BasicDBObject outerUpdateQuery = new BasicDBObject("$push", innerUpdateQuery);

    BasicDBObject query = new BasicDBObject();
    query.put("_id", new ObjectId(projectKey));

    // Execute the update
    UpdateResult result = mongoCollection.updateOne(query, outerUpdateQuery, new UpdateOptions());

    // Construct and send the HTTP response based on Mongo result
    return genHttpResponseFromUpdate(result);
  }

  /**
  * Update a Scene Group in an existing Project.
  */
  @PostMapping("/v1/project/{projectKey}/groups/{groupName}")
  @ResponseBody
  public ResponseEntity<String> updateSceneGroup(
      @PathVariable String projectKey,
      @PathVariable String groupName,
      @RequestBody SceneGroup inpGroup,
      @RequestHeader(name="X-Aesel-Principal", defaultValue="") String aeselPrincipal) {
    logger.info("Attempting to update Scene Group {} in Project {}", groupName, projectKey);

    // Build the Mongo Update Queries
    BasicDBObject innerUpdateQuery = new BasicDBObject();
    if (inpGroup.getName() != null && !(inpGroup.getName().isEmpty())) {
      innerUpdateQuery.put("sceneGroups.$.name", inpGroup.getName());
    }
    if (inpGroup.getDescription() != null) {
      innerUpdateQuery.put("sceneGroups.$.description", inpGroup.getDescription());
    }
    if (inpGroup.getCategory() != null) {
      innerUpdateQuery.put("sceneGroups.$.category", inpGroup.getCategory());
    }
    BasicDBObject outerUpdateQuery = new BasicDBObject("$set", innerUpdateQuery);

    BasicDBObject query = new BasicDBObject();
    query.put("_id", new ObjectId(projectKey));
    query.put("sceneGroups.name", groupName);

    // Execute the update
    UpdateResult result = mongoCollection.updateOne(query, outerUpdateQuery, new UpdateOptions());

    // Construct and send the HTTP response based on Mongo result
    return genHttpResponseFromUpdate(result);
  }

  /**
  * Remove a Scene Group from an existing Project.
  */
  @DeleteMapping("/v1/project/{projectKey}/groups/{groupName}")
  @ResponseBody
  public ResponseEntity<String> deleteSceneGroup(
      @PathVariable String projectKey,
      @PathVariable String groupName,
      @RequestHeader(name="X-Aesel-Principal", defaultValue="") String aeselPrincipal) {
    logger.info("Attempting to remove Scene Group {} from Project {}", groupName, projectKey);

    // Build the Mongo Update Queries
    BasicDBObject sgObject = new BasicDBObject();
    sgObject.put("name", groupName);
    BasicDBObject innerUpdateQuery = new BasicDBObject();
    innerUpdateQuery.put("sceneGroups", sgObject);
    BasicDBObject outerUpdateQuery = new BasicDBObject("$pull", innerUpdateQuery);

    BasicDBObject query = new BasicDBObject();
    query.put("_id", new ObjectId(projectKey));

    // Execute the update
    UpdateResult result = mongoCollection.updateOne(query, outerUpdateQuery, new UpdateOptions());

    // Construct and send the HTTP response based on Mongo result
    return genHttpResponseFromUpdate(result);
  }

  /**
  * Add a Scene to a Scene Group in an existing Project.
  */
  @PutMapping("/v1/project/{projectKey}/groups/{groupName}/scenes/{sceneKey}")
  @ResponseBody
  public ResponseEntity<String> addSceneToSceneGroup(
      @PathVariable String projectKey,
      @PathVariable String groupName,
      @PathVariable String sceneKey,
      @RequestHeader(name="X-Aesel-Principal", defaultValue="") String aeselPrincipal) {
    logger.info("Attempting to add Scene {} Scene Group {} in Project {}",
        sceneKey, groupName, projectKey);

    // Build the Mongo Update Queries
    BasicDBObject innerUpdateQuery = new BasicDBObject();
    innerUpdateQuery.put("sceneGroups.$.scenes", sceneKey);
    BasicDBObject outerUpdateQuery = new BasicDBObject("$push", innerUpdateQuery);

    BasicDBObject query = new BasicDBObject();
    query.put("_id", new ObjectId(projectKey));
    query.put("sceneGroups.name", groupName);

    // Execute the update
    UpdateResult result = mongoCollection.updateOne(query, outerUpdateQuery, new UpdateOptions());

    // Construct and send the HTTP response based on Mongo result
    return genHttpResponseFromUpdate(result);
  }

  /**
  * Remove a Scene from a Scene Group in an existing Project.
  */
  @DeleteMapping("/v1/project/{projectKey}/groups/{groupName}/scenes/{sceneKey}")
  @ResponseBody
  public ResponseEntity<String> removeSceneFromSceneGroup(
      @PathVariable String projectKey,
      @PathVariable String groupName,
      @PathVariable String sceneKey,
      @RequestHeader(name="X-Aesel-Principal", defaultValue="") String aeselPrincipal) {
    logger.info("Attempting to add Scene {} Scene Group {} in Project {}",
        sceneKey, groupName, projectKey);

    // Build the Mongo Update Queries
    BasicDBObject innerUpdateQuery = new BasicDBObject();
    innerUpdateQuery.put("sceneGroups.$.scenes", sceneKey);
    BasicDBObject outerUpdateQuery = new BasicDBObject("$pull", innerUpdateQuery);

    BasicDBObject query = new BasicDBObject();
    query.put("_id", new ObjectId(projectKey));
    query.put("sceneGroups.name", groupName);

    // Execute the update
    UpdateResult result = mongoCollection.updateOne(query, outerUpdateQuery, new UpdateOptions());

    // Construct and send the HTTP response based on Mongo result
    return genHttpResponseFromUpdate(result);
  }

}
