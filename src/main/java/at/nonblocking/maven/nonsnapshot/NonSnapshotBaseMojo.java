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

import at.nonblocking.maven.nonsnapshot.impl.ScmHandlerGitImpl;
import at.nonblocking.maven.nonsnapshot.model.MavenModule;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Base class for NonSnapshot Plugin Mojos.
 * <br/>
 * Does all the parameter handling.
 *
 * @author Juergen Kofler
 */
abstract class NonSnapshotBaseMojo extends AbstractMojo implements Contextualizable {

  private static final Logger LOG = LoggerFactory.getLogger(NonSnapshotBaseMojo.class);
  private static final String DEFAULT_TIMESTAMP_QUALIFIER_PATTERN = "yyyyMMddHHmm";

  protected static final String DIRTY_MODULES_REGISTRY_FILE = "nonSnapshotDirtyModules.txt";

  /**
   * SCM Username
   */
  @Parameter
  private String scmUser;

  /**
   * SCM Password
   */
  @Parameter
  private String scmPassword;

  @Parameter(defaultValue = "true")
  private boolean gitDoPush;

  /**
   * Defer the actual commit until nonsnapshot:commit is called.
   */
  @Parameter(defaultValue = "false")
  private boolean deferPomCommit;

  @Parameter(defaultValue = "false")
  private boolean dontFailOnUpstreamVersionResolution;

  /**
   * Don't let the build fail if the commit of the POM files fails.
   * <br/>
   * Useful if you run this plugin on a CI Server and don't want to let the build fail when
   * a concurrent POM update occurred.
   */
  @Parameter(defaultValue = "false")
  private boolean dontFailOnCommit;

  @Parameter(defaultValue = "${project}")
  private MavenProject mavenProject;

  @Parameter(defaultValue = DEFAULT_TIMESTAMP_QUALIFIER_PATTERN)
  private String timestampQualifierPattern = DEFAULT_TIMESTAMP_QUALIFIER_PATTERN;

  @Parameter
  private List<String> upstreamDependencies;

  /**
   * Generate a property file with a property for all changed projects,
   * which can be used in jenkins for an incremental build (mvn --project ${NONSNAPSHOT_CHANGED_PROJECTS}).
   */
  @Parameter(defaultValue = "false")
  private boolean generateChangedProjectsPropertyFile;

  /**
   * Disable this plugin
   */
  @Parameter(defaultValue = "false")
  private boolean skip;

  @Component(role = MavenPomHandler.class, hint = "default")
  private MavenPomHandler mavenPomHandler;

  @Component(role = ModuleTraverser.class, hint = "default")
  private ModuleTraverser moduleTraverser;

  @Component(role = DependencyTreeProcessor.class, hint = "default")
  private DependencyTreeProcessor dependencyTreeProcessor;

  @Component(role = UpstreamDependencyHandler.class, hint = "default")
  private UpstreamDependencyHandler upstreamDependencyHandler;

  @Component
  private RepositorySystem repositorySystem;

  @Parameter(defaultValue = "${repositorySystemSession}")
  private RepositorySystemSession repositorySystemSession;

  @Parameter(defaultValue = "${project.remoteProjectRepositories}")
  private List<RemoteRepository> remoteRepositories;

  @Parameter(defaultValue = Constants.DEFAULT_INCREMENT_VERSION_PATTERN)
  private String incrementVersionPattern;

  @Parameter(defaultValue = "false", property = "nonsnapshot.appendBranchNameToVersion")
  private boolean appendBranchNameToVersion;

  @Parameter(property = "nonsnapshot.branchName")
  private String branchName;

  /**
   * Replace special symbols (like "/", etc) in new <version> by this string.
   */
  @Parameter(defaultValue = "-")
  private String replaceSpecialSymbolsInVersionBy;

  private List<ProcessedUpstreamDependency> processedUpstreamDependencies;

  private ScmHandler scmHandler;

  private PlexusContainer plexusContainer;

  public void execute() throws MojoExecutionException, MojoFailureException {
    StaticLoggerBinder.getSingleton().setLog(this.getLog());

    if (this.skip) {
      LOG.info("NonSnapshot Plugin has been disabled for this project.");
      return;
    }

    LOG.info("Executing NonSnapshot Plugin for project path: {}", this.mavenProject.getBasedir().getAbsolutePath());

    postProcessParameters();

    internalExecute();
  }

  protected abstract void internalExecute();

  private void postProcessParameters() {
    if (this.scmHandler == null) {
        this.scmHandler = new ScmHandlerGitImpl();
    }

    Properties properties = new Properties();
    properties.setProperty("gitDoPush", String.valueOf(this.gitDoPush));

    this.scmHandler.init(getMavenProject().getBasedir(), this.scmUser, this.scmPassword, properties);

    this.processedUpstreamDependencies = this.upstreamDependencyHandler.processDependencyList(getUpstreamDependencies());
  }

  protected File getDirtyModulesRegistryFile() {
    return new File(this.mavenProject.getBasedir(), DIRTY_MODULES_REGISTRY_FILE);
  }

  @Override
  public void contextualize(Context context) throws ContextException {
    this.plexusContainer = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
  }

  public String getScmUser() {
    return scmUser;
  }

  public void setScmUser(String scmUser) {
    this.scmUser = scmUser;
  }

  public String getScmPassword() {
    return scmPassword;
  }

  public void setScmPassword(String scmPassword) {
    this.scmPassword = scmPassword;
  }

  public boolean isGitDoPush() {
    return gitDoPush;
  }

  public void setGitDoPush(boolean gitDoPush) {
    this.gitDoPush = gitDoPush;
  }

  public boolean isDeferPomCommit() {
    return deferPomCommit;
  }

  public void setDeferPomCommit(boolean deferPomCommit) {
    this.deferPomCommit = deferPomCommit;
  }

  public boolean isDontFailOnUpstreamVersionResolution() {
    return dontFailOnUpstreamVersionResolution;
  }

  public void setDontFailOnUpstreamVersionResolution(boolean dontFailOnUpstreamVersionResolution) {
    this.dontFailOnUpstreamVersionResolution = dontFailOnUpstreamVersionResolution;
  }

  public boolean isDontFailOnCommit() {
    return dontFailOnCommit;
  }

  public void setDontFailOnCommit(boolean dontFailOnCommit) {
    this.dontFailOnCommit = dontFailOnCommit;
  }

  public MavenProject getMavenProject() {
    return mavenProject;
  }

  public void setMavenProject(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  public String getTimestampQualifierPattern() {
    return timestampQualifierPattern;
  }

  public void setTimestampQualifierPattern(String timestampQualifierPattern) {
    this.timestampQualifierPattern = timestampQualifierPattern;
  }

  public List<String> getUpstreamDependencies() {
    return upstreamDependencies;
  }

  public void setUpstreamDependencies(List<String> upstreamDependencies) {
    this.upstreamDependencies = upstreamDependencies;
  }

  public boolean isGenerateChangedProjectsPropertyFile() {
    return generateChangedProjectsPropertyFile;
  }

  public void setGenerateChangedProjectsPropertyFile(boolean generateChangedProjectsPropertyFile) {
    this.generateChangedProjectsPropertyFile = generateChangedProjectsPropertyFile;
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  public MavenPomHandler getMavenPomHandler() {
    return mavenPomHandler;
  }

  public void setMavenPomHandler(MavenPomHandler mavenPomHandler) {
    this.mavenPomHandler = mavenPomHandler;
  }

  public ModuleTraverser getModuleTraverser() {
    return moduleTraverser;
  }

  public void setModuleTraverser(ModuleTraverser moduleTraverser) {
    this.moduleTraverser = moduleTraverser;
  }

  public DependencyTreeProcessor getDependencyTreeProcessor() {
    return dependencyTreeProcessor;
  }

  public void setDependencyTreeProcessor(DependencyTreeProcessor dependencyTreeProcessor) {
    this.dependencyTreeProcessor = dependencyTreeProcessor;
  }

  public UpstreamDependencyHandler getUpstreamDependencyHandler() {
    return upstreamDependencyHandler;
  }

  public void setUpstreamDependencyHandler(UpstreamDependencyHandler upstreamDependencyHandler) {
    this.upstreamDependencyHandler = upstreamDependencyHandler;
  }

  public RepositorySystem getRepositorySystem() {
    return repositorySystem;
  }

  public void setRepositorySystem(RepositorySystem repositorySystem) {
    this.repositorySystem = repositorySystem;
  }

  public RepositorySystemSession getRepositorySystemSession() {
    return repositorySystemSession;
  }

  public void setRepositorySystemSession(RepositorySystemSession repositorySystemSession) {
    this.repositorySystemSession = repositorySystemSession;
  }

  public List<RemoteRepository> getRemoteRepositories() {
    return remoteRepositories;
  }

  public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
    this.remoteRepositories = remoteRepositories;
  }

  public List<ProcessedUpstreamDependency> getProcessedUpstreamDependencies() {
    return processedUpstreamDependencies;
  }

  public void setProcessedUpstreamDependencies(List<ProcessedUpstreamDependency> processedUpstreamDependencies) {
    this.processedUpstreamDependencies = processedUpstreamDependencies;
  }

  public ScmHandler getScmHandler() {
    return scmHandler;
  }

  public void setScmHandler(ScmHandler scmHandler) {
    this.scmHandler = scmHandler;
  }

  public PlexusContainer getPlexusContainer() {
    return plexusContainer;
  }

  public void setPlexusContainer(PlexusContainer plexusContainer) {
    this.plexusContainer = plexusContainer;
  }

  public String getIncrementVersionPattern() {
    return incrementVersionPattern;
  }

  public void setIncrementVersionPattern(String incrementVersionPattern) {
    this.incrementVersionPattern = incrementVersionPattern;
  }

  public boolean isAppendBranchNameToVersion() {
    return appendBranchNameToVersion;
  }

  public void setAppendBranchNameToVersion(boolean appendBranchNameToVersion) {
    this.appendBranchNameToVersion = appendBranchNameToVersion;
  }

  public String getReplaceSpecialSymbolsInVersionBy() {
    return replaceSpecialSymbolsInVersionBy;
  }

  public void setReplaceSpecialSymbolsInVersionBy(String replaceSpecialSymbolsInVersionBy) {
    this.replaceSpecialSymbolsInVersionBy = replaceSpecialSymbolsInVersionBy;
  }

  public String getBranchName() {
    return branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public String getCommitMessageUsingChangedMavenModules(List<MavenModule> changedMavenModules) {
    StringBuilder message = new StringBuilder();
    for (MavenModule changedMavenModule : changedMavenModules) {
      message.append(" - ").append(changedMavenModule.getArtifactId()).append("-");
      if(changedMavenModule.getNewVersion() != null)
        message.append(changedMavenModule.getNewVersion()).append("\n");
      else
        message.append(changedMavenModule.getVersion()).append("\n");
    }
    return ScmHandler.NONSNAPSHOT_COMMIT_MESSAGE_PREFIX + " Version of " + changedMavenModules.size() + " artifacts updated\n\n"
            + "Changes:\n" + message;
  }

  public String getCommitMessageUsingChangedPomFiles(List<File> changedPomFiles) {
    List<MavenModule> changedMavenModules = new ArrayList<>();
    for (File changedPomFile : changedPomFiles) {
      MavenModule mavenModule = getMavenPomHandler().readArtifact(changedPomFile);
      changedMavenModules.add(mavenModule);
    }
    return getCommitMessageUsingChangedMavenModules(changedMavenModules);
  }
}

