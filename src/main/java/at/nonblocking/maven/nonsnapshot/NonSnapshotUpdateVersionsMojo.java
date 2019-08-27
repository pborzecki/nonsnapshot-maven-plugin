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

import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotDependencyResolverException;
import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotPluginException;
import at.nonblocking.maven.nonsnapshot.model.MavenArtifact;
import at.nonblocking.maven.nonsnapshot.model.MavenModule;
import at.nonblocking.maven.nonsnapshot.model.MavenModuleDependency;
import at.nonblocking.maven.nonsnapshot.model.UpdatedUpstreamMavenArtifact;
import at.nonblocking.maven.nonsnapshot.version.NewVersionResolver;
import com.google.common.io.Files;
import org.apache.maven.model.Model;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Goal of this Plugin. <br/>
 * <br/>
 * Checks the version number and manipulates it if necessary (if changes were found).
 * <br/>
 * Updates the version of upstream modules.
 * <br/>
 * Commits the POM files if deferPomCommit is false.
 *
 * @author Juergen Kofler
 */
@Mojo(name = "updateVersions", aggregator = true)
public class NonSnapshotUpdateVersionsMojo extends NonSnapshotBaseMojo {

  private static Logger LOG = LoggerFactory.getLogger(NonSnapshotUpdateVersionsMojo.class);

  private static String LINE_SEPARATOR = System.getProperty("line.separator");

  @Override
  protected void internalExecute() {
    List<Model> mavenModels = getModuleTraverser().findAllModules(getMavenProject(), getMavenProject().getActiveProfiles());

    List<MavenModule> mavenModules = buildModules(mavenModels);

    MavenModule rootModule = mavenModules.get(0);

    getDependencyTreeProcessor().buildDependencyTree(mavenModules);

    markDirtyWhenRevisionChangedOrInvalidQualifier(mavenModules);

    if (getUpstreamDependencies() != null) {
      updateUpstreamArtifacts(mavenModules);
    }

    //Recursively mark artifacts dirty
    boolean changes = getDependencyTreeProcessor().markAllArtifactsDirtyWithDirtyDependencies(mavenModules);
    while (changes) {
      changes = getDependencyTreeProcessor().markAllArtifactsDirtyWithDirtyDependencies(mavenModules);
    }

    setNextRevisionOnDirtyArtifacts(mavenModules);

    dumpArtifactTreeToLog(rootModule);

    writeAndCommitArtifacts(mavenModules);
  }

  private List<MavenModule> buildModules(List<Model> mavenModels) {
    List<MavenModule> mavenModules = new ArrayList<>();

    for (Model model : mavenModels) {
      MavenModule module = getMavenPomHandler().readArtifact(model);
      mavenModules.add(module);
    }

    return mavenModules;
  }

    protected void writeAndCommitArtifacts(List<MavenModule> mavenModules) {
        List<File> pomsToCommit = new ArrayList<>();
        List<MavenModule> modulesToCommit = new ArrayList<>();

        for (MavenModule mavenModule : mavenModules) {
            if (mavenModule.isDirty() && mavenModule.getNewVersion() != null) {
                getMavenPomHandler().updateArtifact(mavenModule);
                LOG.debug("Add module to dirty registry list: {}", mavenModule.getPomFile().getAbsolutePath());
                pomsToCommit.add(mavenModule.getPomFile());
                modulesToCommit.add(mavenModule);
            }
        }

        if (isGenerateChangedProjectsPropertyFile()) {
            generateChangedProjectsPropertyFile(pomsToCommit);
        }

        if (pomsToCommit.size() > 0) {
            writeDirtyModulesRegistry(pomsToCommit);
            if (!isDeferPomCommit()) {
                LOG.info("Committing {} POM files", pomsToCommit.size());
                String message = getCommitMessageUsingChangedMavenModules(modulesToCommit);
                getScmHandler().commitFiles(pomsToCommit, message);
            } else {
                LOG.info("Deferring the POM commit. Execute nonsnapshot:commit to actually commit the changes.");
            }
        } else {
            LOG.info("Modules are up-to-date. No versions updated.");
        }
    }

  protected void markDirtyWhenRevisionChangedOrInvalidQualifier(List<MavenModule> mavenModules) {
    for (MavenModule mavenModule : mavenModules) {
      if (mavenModule.getVersion() == null) {
        LOG.info("No version found for artifact {}:{}. Assigning a new version.", mavenModule.getGroupId(), mavenModule.getArtifactId());
        mavenModule.setDirty(true);

      } else if (mavenModule.getVersion().startsWith("${")) {
        LOG.info("Version property found for artifact {}:{}. Assigning a new version.", mavenModule.getGroupId(), mavenModule.getArtifactId());
        mavenModule.setDirty(true);

      } else {
        String[] versionParts = mavenModule.getVersion().split("-");
        String qualifierString = null;
        if (versionParts.length > 1) {
          qualifierString = versionParts[versionParts.length - 1];
        }

        if (qualifierString != null && qualifierString.equals("SNAPSHOT")) {
          LOG.info("Snapshot version found for artifact {}:{}. Assigning a new version.", mavenModule.getGroupId(), mavenModule.getArtifactId());
          mavenModule.setDirty(true);

        } else {
          if (getScmHandler().checkChangesSinceLastUpdate(getChangeScope(mavenModule))) {
            LOG.info("Module {}:{}: There were commits after last plugin increment. Assigning a new version.",
                    mavenModule.getGroupId(), mavenModule.getArtifactId());
            mavenModule.setDirty(true);
          }
        }
      }
    }
  }

  private static File getChangeScope(MavenModule mavenModule) {
    try {
      if(Files.toString(mavenModule.getPomFile(), StandardCharsets.UTF_8).contains("<packaging>pom</packaging>")){
        return mavenModule.getPomFile();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return mavenModule.getPomFile().getParentFile();
  }

  protected void updateUpstreamArtifacts(List<MavenModule> mavenModules) {
    for (MavenModule mavenModule : mavenModules) {
      //Parent
      if (mavenModule.getParent() != null) {
        UpdatedUpstreamMavenArtifact updatedUpstreamMavenArtifactParent = updateUpstreamArtifact(mavenModule.getParent());
        if (updatedUpstreamMavenArtifactParent != null) {
          mavenModule.setParent(updatedUpstreamMavenArtifactParent);
        }
      }

      //Dependencies
      for (MavenModuleDependency moduleDependency : mavenModule.getDependencies()) {
        UpdatedUpstreamMavenArtifact updatedUpstreamMavenArtifactDep = updateUpstreamArtifact(moduleDependency.getArtifact());
        if (updatedUpstreamMavenArtifactDep != null) {
          moduleDependency.setArtifact(updatedUpstreamMavenArtifactDep);
        }
      }
    }
  }

  protected UpdatedUpstreamMavenArtifact updateUpstreamArtifact(MavenArtifact upstreamArtifact) {
    if (!(upstreamArtifact instanceof MavenModule)) {
      ProcessedUpstreamDependency upstreamDependency = getUpstreamDependencyHandler().findMatch(upstreamArtifact, getProcessedUpstreamDependencies());
      if (upstreamDependency != null) {
        LOG.debug("Upstream dependency found: {}:{}", upstreamArtifact.getGroupId(), upstreamArtifact.getArtifactId());

        try {
          String latestVersion = getUpstreamDependencyHandler().resolveLatestVersion(upstreamArtifact, upstreamDependency, getRepositorySystem(), getRepositorySystemSession(), getRemoteRepositories());
          if (latestVersion != null) {
            LOG.info("Found newer version for upstream dependency {}:{}: {}", new Object[]{upstreamArtifact.getGroupId(), upstreamArtifact.getArtifactId(), latestVersion});
            return new UpdatedUpstreamMavenArtifact(upstreamArtifact.getGroupId(), upstreamArtifact.getArtifactId(), upstreamArtifact.getVersion(), latestVersion);
          }
        } catch (NonSnapshotDependencyResolverException e) {
          if (isDontFailOnUpstreamVersionResolution()) {
            LOG.warn("Upstream dependency resolution failed (cannot update {}:{}). Error: {}",
                new Object[]{upstreamArtifact.getGroupId(), upstreamArtifact.getArtifactId(), e.getMessage()});
          } else {
            throw e;
          }
        }
      }
    }

    return null;
  }

  private void setNextRevisionOnDirtyArtifacts(List<MavenModule> mavenModules) {
    for (MavenModule mavenModule : mavenModules) {
      File modulesPath;
      try {
        modulesPath = mavenModule.getPomFile().getParentFile().getCanonicalFile();
      } catch (IOException e) {
        throw new NonSnapshotPluginException("Unexpected IO exception", e);
      }

      if (mavenModule.isDirty()) {
        if (!getScmHandler().isWorkingCopy(modulesPath)) {
          throw new NonSnapshotPluginException("Module path is no working directory: " + modulesPath);
        }
        String branch = getBranchName() != null ? getBranchName() : getScmHandler().getBranchName();
        NewVersionResolver resolver = new NewVersionResolver(isAppendBranchNameToVersion(),
                getIncrementVersionPattern(), getReplaceSpecialSymbolsInVersionBy());
        String newVersion = resolver.resolveNewVersion(mavenModule.getVersion(), branch);
        mavenModule.setNewVersion(newVersion);
        LOG.info("{}:{}:{} -> {}", new Object[]{
                mavenModule.getGroupId(),
                mavenModule.getArtifactId(),
                mavenModule.getVersion(),
                newVersion});
      }
    }
  }

  private void writeDirtyModulesRegistry(List<File> pomFileList) {
    File dirtyModulesRegistryFile = getDirtyModulesRegistryFile();
    LOG.info("Writing dirty modules registry to: {}", dirtyModulesRegistryFile.getAbsolutePath());

    try (PrintWriter writer = new PrintWriter(new FileOutputStream(dirtyModulesRegistryFile, false))) {
      for (File pomFile : pomFileList) {
        String relativeModuleDir = PathUtil.relativePath(getMavenProject().getBasedir(), pomFile.getParentFile());
        if (relativeModuleDir.isEmpty()) {
          relativeModuleDir = ".";
        }
        writer.write(relativeModuleDir + LINE_SEPARATOR);
      }

    } catch (IOException e) {
      throw new NonSnapshotPluginException("Failed to write text file with POMs to commit!", e);
    }
  }

  protected void generateChangedProjectsPropertyFile(List<File> pomFileList) {
    String projectPaths = createProjectPathsString(pomFileList);
    if (projectPaths.isEmpty()) {
      projectPaths = "."; //An empty property wont work on Jenkins
    }

    File propertyFile = new File(getMavenProject().getBasedir(), "nonsnapshotChangedProjects.properties");
    LOG.info("Writing changed projects to property file: {}", propertyFile.getAbsolutePath());

    try (PrintWriter writer = new PrintWriter(propertyFile)) {
      writer.write("#Property with changed projects generated by nonsnapshot-maven-plugin\n");
      writer.write("#Can be used together with the Jenkins EnvInject plugin to build changed projects only:\n");
      writer.write("#mvn --projects ${nonsnapshotChangedProjects} install\n");
      writer.write("nonsnapshotChangedProjects=" + projectPaths + "\n");
      writer.close();

    } catch (IOException e) {
      LOG.error("Failed to write changed projects property file!", e);
    }
  }

  private String createProjectPathsString(List<File> pomFileList) {
    StringBuilder projectPaths = new StringBuilder();

    try {
      for (File pomFile : pomFileList) {
        String relativeModuleDir = PathUtil.relativePath(getMavenProject().getBasedir(), pomFile.getParentFile());
        if (relativeModuleDir.isEmpty()) {
          relativeModuleDir = ".";
        }
        if (projectPaths.length() != 0) {
          relativeModuleDir = "," + relativeModuleDir;
        }
        projectPaths.append(relativeModuleDir);
      }

      return projectPaths.toString();

    } catch (IOException e) {
      throw new NonSnapshotPluginException("Failed determine changed project paths!", e);
    }
  }

  private void dumpArtifactTreeToLog(MavenModule rootModule) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getDependencyTreeProcessor().printMavenModuleTree(rootModule, new PrintStream(baos));
    LOG.info("\n" + baos.toString());
  }
}
