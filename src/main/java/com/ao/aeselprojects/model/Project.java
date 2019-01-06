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

package com.ao.aeselprojects.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "projects")
public class Project {

  @Id
  public String id;
  public String name;
  public String description;
  public String category;
  public String thumbnail;
  public String user;
  public boolean isPublic;
  public List<String> tags;
  public List<SceneGroup> sceneGroups;
  public List<String> assetCollectionIds;

}
