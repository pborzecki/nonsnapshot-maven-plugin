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
package at.nonblocking.maven.nonsnapshot.impl;

import at.nonblocking.maven.nonsnapshot.*;
import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotDependencyResolverException;
import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotPluginException;
import at.nonblocking.maven.nonsnapshot.model.MavenArtifact;
import at.nonblocking.maven.nonsnapshot.model.MavenModule;
import at.nonblocking.maven.nonsnapshot.model.MavenModuleDependency;
import at.nonblocking.maven.nonsnapshot.version.VersionParser;
import at.nonblocking.maven.nonsnapshot.version.Version;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Default implementation of UpstreamDependencyHandler
 *
 * @author Juergen Kofler
 */
@Component(role = UpstreamDependencyHandler.class, hint = "default")
public class UpstreamDependencyHandlerDefaultImpl implements UpstreamDependencyHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UpstreamDependencyHandlerDefaultImpl.class);

  private ProcessedUpstreamDependency makeProcessedUpstreamDependencyFromMavenArtifact(MavenArtifact artifact)
  {
    VersionParser versionParser = new VersionParser(Constants.DEFAULT_UPSTREAM_DEPENDENCY_VERSION_PATTERN);
    Version version;
    try {
      version = versionParser.parse(artifact.getVersion());
    } catch (IllegalArgumentException e) {
      throw new NonSnapshotPluginException("Illegal upstream dependency version number: " + artifact.getVersion() + " in artifact id: " + artifact.toString() + " - details: " + e.getMessage());
    }
    Pattern groupIdPattern = createPattern(artifact.getGroupId());
    Pattern artifactIdPattern = createPattern(artifact.getArtifactId());
    return new ProcessedUpstreamDependency(groupIdPattern, artifactIdPattern, version);
  }

  private ProcessedUpstreamDependency makeProcessedUpstreamDependencyFromUpstreamDependencyString(String upstreamDependencyString)
  {
    if (upstreamDependencyString.trim().isEmpty()) {
      throw new NonSnapshotPluginException("Illegal upstream dependency: " + upstreamDependencyString);
    }

    return makeProcessedUpstreamDependencyFromMavenArtifact(new MavenArtifact(upstreamDependencyString));
  }

  private void processBomUpstreamDependency(String upstreamDependencyString,
                                            List<ProcessedUpstreamDependency> processedUpstreamDependencies,
                                            MavenPomHandler mavenPomHandler,
                                            RepositorySystem repositorySystem,
                                            RepositorySystemSession repositorySystemSession,
                                            List<RemoteRepository> remoteRepositories)
  {
    try {
      LOG.info("Resolving artifact: {}", upstreamDependencyString);
      ArtifactRequest artifactRequest = new ArtifactRequest(new DefaultArtifact(upstreamDependencyString), remoteRepositories, null);
      ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
      LOG.info("Artifact {} resolved parsing its dependencies", upstreamDependencyString);
      MavenModule bomMavenModule = mavenPomHandler.readArtifact(result.getArtifact().getFile());
      for(MavenModuleDependency indirectDependency : bomMavenModule.getDependencies()) {
        MavenArtifact indirectDependencyArtifact = indirectDependency.getArtifact();
        String indirectDependencyScope = indirectDependency.getScope();
        processedUpstreamDependencies.add(makeProcessedUpstreamDependencyFromMavenArtifact(indirectDependencyArtifact));
        LOG.info(
          "Indirect dependency: {} (scope: {}) found and added", indirectDependencyArtifact.toString(), indirectDependencyScope);
        if(indirectDependencyArtifact.getType() != null && indirectDependencyScope != null &&
           indirectDependencyArtifact.getType().equals("pom") && indirectDependencyScope.equals("import"))
        {
          LOG.info(
            "Indirect dependency: {} is of pom type and has import scope - it will be treated as bom upstream dependency",
            indirectDependencyArtifact.toString()
          );
          processBomUpstreamDependency(
            indirectDependencyArtifact.toString(), processedUpstreamDependencies, mavenPomHandler, repositorySystem, repositorySystemSession, remoteRepositories);
        }


      }
    } catch (ArtifactResolutionException e) {
      throw new NonSnapshotDependencyResolverException("Couldn't resolve bom dependency: " + upstreamDependencyString, e);
    }
  }

  @Override
  public List<ProcessedUpstreamDependency> processDependencyList(List upstreamDependencies,
                                                                 MavenPomHandler mavenPomHandler,
                                                                 RepositorySystem repositorySystem,
                                                                 RepositorySystemSession repositorySystemSession,
                                                                 List<RemoteRepository> remoteRepositories) {
    LOG.info("Collecting upstream dependencies");

    if (upstreamDependencies == null || upstreamDependencies.isEmpty()) {
      return null;
    }

    List<ProcessedUpstreamDependency> processedUpstreamDependencies = new ArrayList<>(upstreamDependencies.size());

    for (Object upstreamDependency : upstreamDependencies) {
      boolean isUpstreamBomDependency = false;
      String upstreamDependencyString;

      if(upstreamDependency instanceof BomDependency) {
        BomDependency dependency = (BomDependency) upstreamDependency;
        upstreamDependencyString = dependency.getUpstreamDependency();
        isUpstreamBomDependency = true;
      }
      else if(upstreamDependency instanceof String) {
        upstreamDependencyString = (String) upstreamDependency;
      }
      else {
        throw new NonSnapshotPluginException("Illegal upstream dependency type: " + upstreamDependency.toString());
      }

      processedUpstreamDependencies.add(makeProcessedUpstreamDependencyFromUpstreamDependencyString(upstreamDependencyString));

      if(isUpstreamBomDependency) {
        LOG.info("Bom upstream dependency: {} found and added", upstreamDependencyString);
        processBomUpstreamDependency(
                upstreamDependencyString, processedUpstreamDependencies, mavenPomHandler, repositorySystem, repositorySystemSession, remoteRepositories);
      } else
        LOG.info("Regular upstream dependency: {} found and added", upstreamDependencyString);
    }

    return processedUpstreamDependencies;
  }

  private Pattern createPattern(String regex) {
    regex = regex.replaceAll("\\.", "\\\\.");
    regex = regex.replaceAll("\\*", ".*");
    return Pattern.compile(regex);
  }

  @Override
  public ProcessedUpstreamDependency findMatch(MavenArtifact mavenArtifact, List<ProcessedUpstreamDependency> upstreamDependencies) {
    if (upstreamDependencies == null) {
      return null;
    }

    for (ProcessedUpstreamDependency upstreamDependency : upstreamDependencies) {
      if (upstreamDependency.getGroupPattern().matcher(mavenArtifact.getGroupId()).matches()
          && upstreamDependency.getArtifactPattern().matcher(mavenArtifact.getArtifactId()).matches()) {
        return upstreamDependency;
      }
    }

    return null;
  }

  @Override
  public String resolveLatestVersion(MavenArtifact mavenArtifact, ProcessedUpstreamDependency upstreamDependency,
                                     RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession,
                                     List<RemoteRepository> remoteRepositories) throws NonSnapshotDependencyResolverException {

    String currentVersion = mavenArtifact.getVersion();
    if (currentVersion == null) {
      throw new IllegalArgumentException("Empty version in " + mavenArtifact);
    }
    if (currentVersion.contains("$")) {
      currentVersion = "0.0.0";
    } else if (currentVersion.endsWith("-SNAPSHOT")) {
      currentVersion = currentVersion.split("-")[0];
    }

    Version upstreamDependencyVersion = upstreamDependency.getVersion();
    if(upstreamDependencyVersion.getIsItSnapshot()) {
      LOG.info("Latest version resolution for dependency {}:{} will be skipped cause SNAPSHOT version has been provided as upstream dependency ({})",
              new Object[]{mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), upstreamDependencyVersion});
      return upstreamDependencyVersion.toString();
    }

    // TODO: bellow code will be refactored soon, due to version manipulation. Refactoring just begun, but is not complete yet...
    String versionPrefix;
    String versionQuery;

    if (upstreamDependencyVersion.getMinorVersion() != null) {
      versionPrefix = upstreamDependencyVersion.getMajorVersion() + "." + upstreamDependencyVersion.getMiddleVersion() + "." + upstreamDependencyVersion.getMinorVersion();
      String nextIncrement = upstreamDependencyVersion.getMajorVersion() + "." + upstreamDependencyVersion.getMiddleVersion() + "." + (upstreamDependencyVersion.getMinorVersion() + 1);
      versionQuery = mavenArtifact.getGroupId() + ":" + mavenArtifact.getArtifactId() + ":(" + currentVersion + "," + nextIncrement + ")";
    } else if (upstreamDependencyVersion.getMiddleVersion() != null) {
      versionPrefix = upstreamDependencyVersion.getMajorVersion() + "." + upstreamDependencyVersion.getMiddleVersion();
      String nextMinor = upstreamDependencyVersion.getMajorVersion() + "." + (upstreamDependencyVersion.getMiddleVersion() + 1) + ".0";
      versionQuery = mavenArtifact.getGroupId() + ":" + mavenArtifact.getArtifactId() + ":(" + currentVersion + "," + nextMinor + ")";
    } else if (upstreamDependencyVersion.getMajorVersion() != null) {
      versionPrefix = String.valueOf(upstreamDependencyVersion.getMajorVersion());
      String nextMajor = (upstreamDependencyVersion.getMajorVersion() + 1) + ".0.0";
      versionQuery = mavenArtifact.getGroupId() + ":" + mavenArtifact.getArtifactId() + ":(" + currentVersion + "," + nextMajor + ")";
    } else {
      versionPrefix = "";
      versionQuery = mavenArtifact.getGroupId() + ":" + mavenArtifact.getArtifactId() + ":(" + currentVersion + ",)";
    }

    Artifact aetherArtifact = new DefaultArtifact(versionQuery);

    VersionRangeRequest rangeRequest = new VersionRangeRequest();
    rangeRequest.setArtifact(aetherArtifact);
    rangeRequest.setRepositories(remoteRepositories);

    try {
      LOG.debug("Resolving versions for {}", versionQuery);
      VersionRangeResult result = repositorySystem.resolveVersionRange(repositorySystemSession, rangeRequest);
      LOG.debug("Found versions for {}: {}", versionQuery, result);

      List<org.eclipse.aether.version.Version> versions = result.getVersions();
      Collections.reverse(versions);

      for (org.eclipse.aether.version.Version version : versions) {
        String versionStr = version.toString();
        if (!versionStr.endsWith("-SNAPSHOT") && versionStr.startsWith(versionPrefix)) {
          return versionStr;
        }
      }

      return null;

    } catch (VersionRangeResolutionException e) {
      throw new NonSnapshotDependencyResolverException("Couldn't resolve latest upstream version for: " + versionQuery + ". Keeping current version " + currentVersion, e);
    }
  }


}
