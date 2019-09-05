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
package at.nonblocking.maven.nonsnapshot;

import at.nonblocking.maven.nonsnapshot.version.Version;

import java.util.regex.Pattern;

/**
 * Upstream dependency model
 *
 * @author Juergen Kofler
 */
public class ProcessedUpstreamDependency {

  private Pattern groupPattern;
  private Pattern artifactPattern;
  private Version version;

  public ProcessedUpstreamDependency(Pattern groupPattern, Pattern artifactPattern, Version version) {
    this.groupPattern = groupPattern;
    this.artifactPattern = artifactPattern;
    this.version = version;
  }

  public Pattern getGroupPattern() {
    return groupPattern;
  }

  public void setGroupPattern(Pattern groupPattern) {
    this.groupPattern = groupPattern;
  }

  public Pattern getArtifactPattern() {
    return artifactPattern;
  }

  public void setArtifactPattern(Pattern artifactPattern) {
    this.artifactPattern = artifactPattern;
  }

  public Version getVersion() { return version; }

  public void setVersionMajor(Version version) { this.version = version; }

  @Override
  public String toString() {
    return "UpstreamDependency{" +
        "groupPattern=" + groupPattern +
        ", artifactPattern=" + artifactPattern +
        ", version=" + version.toString() +
        '}';
  }
}
