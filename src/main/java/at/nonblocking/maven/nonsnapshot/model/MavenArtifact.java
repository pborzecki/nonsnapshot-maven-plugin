/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.nonblocking.maven.nonsnapshot.model;

/**
 * A common Maven artifact, which might or might not be located in the Workspace.
 *
 * @author Juergen Kofler
 */
public class MavenArtifact {

  private String groupId;
  private String artifactId;
  private String type;
  private String version;

  public MavenArtifact(String artifactDescription) {
    String[] parts = artifactDescription.split(":");
    if (parts.length < 2)
      throw new IllegalArgumentException("Wrong maven artifact description: " + artifactDescription);

    groupId = parts[0];
    artifactId = parts[1];
    if(parts.length == 4) {
      type = parts[2];
      version = parts[3];
    }
    else if(parts.length == 3) {
      version = parts[2];
    }
  }

  public MavenArtifact(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.type = null;
    this.version = version;
  }

  public MavenArtifact(String groupId, String artifactId, String type, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.type = type;
    this.version = version;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(64);
    builder.append(groupId).append(":").append(artifactId);
    if(type != null)
        builder.append(":").append(type);
    if(version != null)
      builder.append(":").append(version);
    return builder.toString();
  }
}
