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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
* Rest Controller defining the Project API.
* Responsible for handling and responding to all Project API Requests.
*/
@Controller
public class ProjectController {

  // Spring Data Mongo Repository allowing access to standard Mongo operations
  @Autowired
  ProjectRepository projects;

  // Asset Controller Logger
  private static final Logger logger =
      LogManager.getLogger("aeselprojects.ProjectController");

  /**
  * Get a Project.
  */
  @GetMapping("/v1/project/{id}")
  @ResponseBody
  public ResponseEntity<Project> getProject(@PathVariable String id) {
    logger.info("Responding to Project Get Request");
    HttpStatus returnCode = HttpStatus.OK;
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", "application/json");
    Project returnProject = null;
    Optional<Project> existingProject = projects.findById(id);
    if (existingProject.isPresent()) {
      returnProject = existingProject.get();
    } else {
      returnCode = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
      logger.debug("No Project found");
      returnProject = new Project();
    }

    // Create and return the new HTTP Response
    return new ResponseEntity<Project>(returnProject, responseHeaders, returnCode);
  }

  /**
  * Query for Projects.
  */
  @GetMapping("/v1/project")
  @ResponseBody
  public ResponseEntity<List<Project>> findProjects(
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "category", defaultValue = "") String category,
      @RequestParam(value = "tag", defaultValue = "") String tag,
      @RequestParam(value = "num_records", defaultValue = "10") int recordsInPage,
      @RequestParam(value = "page", defaultValue = "0") int pageNum) {
    logger.info("Responding to Project Get Request");
    HttpStatus returnCode = HttpStatus.OK;
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", "application/json");
    List<Project> returnProjects = null;
    Pageable pageable = new PageRequest(pageNum, recordsInPage);
    if (name != "") {
      returnProjects = projects.findByName(name, pageable);
    } else if (category != "" && tag != "") {
      returnProjects = projects.findByCategoryAndTagsIn(category, new HashSet<String>(Arrays.asList(tag)), pageable);
    } else if (category != "") {
      returnProjects = projects.findByCategory(category, pageable);
    } else if (tag != "") {
      returnProjects = projects.findByTagsIn(new HashSet<String>(Arrays.asList(tag)), pageable);
    } else {
      returnProjects = projects.findAll(pageable).getContent();
    }
    if (returnProjects.size() == 0 && returnCode == HttpStatus.OK) {
      returnCode = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
      logger.debug("No Asset Collection found");
      returnProjects = new ArrayList<Project>();
    }

    // Create and return the new HTTP Response
    return new ResponseEntity<List<Project>>(returnProjects, responseHeaders, returnCode);
  }

  /**
  * Delete a Project.
  */
  @DeleteMapping("/v1/project/{id}")
  @ResponseBody public ResponseEntity<String> deleteProject(
      @PathVariable String id) {
    logger.info("Responding to Project Delete Request");
    HttpStatus returnCode = HttpStatus.OK;
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", "application/json");
    projects.deleteById(id);
    return new ResponseEntity<String>(id, responseHeaders, returnCode);
  }

  /**
   * Create a new Project.
   */
  @PostMapping("/v1/project")
  @ResponseBody
  public ResponseEntity<Project> createProject(
      @RequestBody Project inpProject) {
    logger.info("Responding to Project Create Request");
    HttpStatus returnCode = HttpStatus.OK;
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", "application/json");
    Project responseProject = projects.insert(inpProject);
    return new ResponseEntity<Project>(responseProject, responseHeaders, returnCode);
  }

  /**
   * Update an existing Project.
   */
  @PostMapping("/v1/project/{id}")
  @ResponseBody
  public ResponseEntity<Project> updateProject(
      @PathVariable String id,
      @RequestBody Project inpProject) {
    logger.info("Responding to Project Create Request");
    HttpStatus returnCode = HttpStatus.OK;
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", "application/json");
    inpProject.setId(id);
    Project responseProject = projects.save(inpProject);
    return new ResponseEntity<Project>(responseProject, responseHeaders, returnCode);
  }

}
