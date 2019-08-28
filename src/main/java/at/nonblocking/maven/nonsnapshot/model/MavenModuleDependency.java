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
 * A dependency of a Maven module
 *
 * @author Juergen Kofler
 */
public class MavenModuleDependency {

  private int versionLocation;
  private MavenArtifact artifact;
  private String scope = null;

  public MavenModuleDependency(int versionLocation, MavenArtifact artifact) {
    this.versionLocation = versionLocation;
    this.artifact = artifact;
  }

  public MavenModuleDependency(int versionLocation, MavenArtifact artifact, String scope) {
    this.versionLocation = versionLocation;
    this.artifact = artifact;
    this.scope = scope;
  }

  public int getVersionLocation() {
    return versionLocation;
  }

  public void setVersionLocation(int versionLocation) {
    this.versionLocation = versionLocation;
  }

  public MavenArtifact getArtifact() {
    return artifact;
  }

  public void setArtifact(MavenArtifact artifact) {
    this.artifact = artifact;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

}
