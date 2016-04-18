package at.nonblocking.maven.nonsnapshot;

import at.nonblocking.maven.nonsnapshot.model.MavenModule;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NonSnapshotUpdateVersionsMojoTest {

  private NonSnapshotUpdateVersionsMojo nonSnapshotMojo = new NonSnapshotUpdateVersionsMojo();
  private ModuleTraverser mockModuleTraverser = mock(ModuleTraverser.class);
  private DependencyTreeProcessor mockDependencyTreeProcessor = mock(DependencyTreeProcessor.class);
  private MavenPomHandler mockMavenPomHandler = mock(MavenPomHandler.class);
  private ScmHandler mockScmHandler = mock(ScmHandler.class);
  private UpstreamDependencyHandler mockUpstreamDependencyHandler = mock(UpstreamDependencyHandler.class);

  @BeforeClass
  public static void setupLog() {
    StaticLoggerBinder.getSingleton().setLog(new DebugSystemStreamLog());
  }

  @Before
  public void setupMojo() {
    MavenProject mavenProject = new MavenProject();
    mavenProject.setFile(new File("target/pom.xml"));
    this.nonSnapshotMojo.setMavenProject(mavenProject);

    this.nonSnapshotMojo.setBaseVersion("1.0.13");
    this.nonSnapshotMojo.setScmUser("foo");
    this.nonSnapshotMojo.setScmPassword("bar");
    this.nonSnapshotMojo.setDeferPomCommit(false);
    this.nonSnapshotMojo.setModuleTraverser(mockModuleTraverser);
    this.nonSnapshotMojo.setDependencyTreeProcessor(this.mockDependencyTreeProcessor);
    this.nonSnapshotMojo.setMavenPomHandler(this.mockMavenPomHandler);
    this.nonSnapshotMojo.setScmHandler(this.mockScmHandler);
    this.nonSnapshotMojo.setUpstreamDependencyHandler(this.mockUpstreamDependencyHandler);
  }

  @Test
  public void testUpdateTimestampQualifiers() throws Exception {
    String pattern = "yyyyMMdd";
    Date currentTime = new Date();
    String currentTimestamp = new SimpleDateFormat(pattern).format(currentTime);

    Model model1 = new Model();
    Model model2 = new Model();
    Model model3 = new Model();
    Model model4 = new Model();
    Model model5 = new Model();

    File pom1 = new File("test1/pom.xm");
    File pom2 = new File("test2/pom.xm");
    File pom3 = new File("test3/pom.xm");
    File pom4 = new File("test4/pom.xm");
    File pom5 = new File("test5/pom.xm");

    MavenModule wsArtifact1 = new MavenModule(pom1, "nonblocking.at", "test1", "1.0.0-SNAPSHOT"); // Invalid version
    MavenModule wsArtifact2 = new MavenModule(pom2, "nonblocking.at", "test2", "1.1.0-" + currentTimestamp);
    MavenModule wsArtifact3 = new MavenModule(pom3, "nonblocking.at", "test3", null); // Invalid version
    MavenModule wsArtifact4 = new MavenModule(pom4, "nonblocking.at", "test3", "2.1.0-FIX1-1234");
    MavenModule wsArtifact5 = new MavenModule(pom5, "nonblocking.at", "test3", "${test.version}"); // Invalid version

    List<MavenModule> artifactList = new ArrayList<>();
    artifactList.add(wsArtifact1);
    artifactList.add(wsArtifact2);
    artifactList.add(wsArtifact3);
    artifactList.add(wsArtifact4);
    artifactList.add(wsArtifact5);

    MavenProject mavenProject = new MavenProject();
    mavenProject.setFile(new File("target"));

    when(this.mockModuleTraverser.findAllModules(mavenProject, Collections.<Profile>emptyList())).thenReturn(Arrays.asList(model1, model2, model3, model4, model5));
    when(this.mockMavenPomHandler.readArtifact(model1)).thenReturn(wsArtifact1);
    when(this.mockMavenPomHandler.readArtifact(model2)).thenReturn(wsArtifact2);
    when(this.mockMavenPomHandler.readArtifact(model3)).thenReturn(wsArtifact3);
    when(this.mockMavenPomHandler.readArtifact(model4)).thenReturn(wsArtifact4);
    when(this.mockMavenPomHandler.readArtifact(model5)).thenReturn(wsArtifact5);

    when(this.mockScmHandler.checkChangesSinceRevision(pom2.getParentFile(), "1234")).thenReturn(false);
    when(this.mockScmHandler.checkChangesSinceRevision(pom4.getParentFile(), "1234")).thenReturn(true);

    when(this.mockScmHandler.isWorkingCopy(any(File.class))).thenReturn(true);

    this.nonSnapshotMojo.setTimestampQualifierPattern(pattern);
    this.nonSnapshotMojo.execute();

    assertEquals("1.0.13-" + currentTimestamp, wsArtifact1.getNewVersion());
    assertNull(wsArtifact2.getNewVersion());
    assertNotNull(wsArtifact3.getNewVersion());
    assertEquals("1.0.13-" + currentTimestamp, wsArtifact4.getNewVersion());
    assertEquals("1.0.13-" + currentTimestamp, wsArtifact5.getNewVersion());

    InOrder inOrder = inOrder(this.mockDependencyTreeProcessor, this.mockMavenPomHandler, this.mockScmHandler, this.mockModuleTraverser);

    inOrder.verify(this.mockMavenPomHandler).readArtifact(model1);
    inOrder.verify(this.mockMavenPomHandler).readArtifact(model2);
    inOrder.verify(this.mockMavenPomHandler).readArtifact(model3);
    inOrder.verify(this.mockMavenPomHandler).readArtifact(model4);
    inOrder.verify(this.mockMavenPomHandler).readArtifact(model5);

    inOrder.verify(this.mockDependencyTreeProcessor).buildDependencyTree(artifactList);

    when(this.mockScmHandler.checkChangesSinceRevision(pom2.getParentFile(), "1234")).thenReturn(false);
    when(this.mockScmHandler.checkChangesSinceRevision(pom4.getParentFile(), "1234")).thenReturn(true);

    inOrder.verify(this.mockMavenPomHandler, times(1)).updateArtifact(wsArtifact1);
    verify(this.mockMavenPomHandler, never()).updateArtifact(wsArtifact2);
    verify(this.mockMavenPomHandler, times(1)).updateArtifact(wsArtifact3);
    inOrder.verify(this.mockMavenPomHandler, times(1)).updateArtifact(wsArtifact4);
    inOrder.verify(this.mockMavenPomHandler, times(1)).updateArtifact(wsArtifact5);

    inOrder.verify(this.mockScmHandler).commitFiles(Arrays.asList(pom1, pom3, pom4, pom5),
            NonSnapshotUpdateVersionsMojo.messageFormat(Arrays.asList(wsArtifact1, wsArtifact3, wsArtifact4, wsArtifact5)));
  }

  @Test
  public void testUpdateDeferCommit() throws Exception {
    Model model1 = new Model();

    File pom1 = new File("test1/pom.xm");

    File dirtyModulesRegistry = new File("target/nonSnapshotDirtyModules.txt");
    if (dirtyModulesRegistry.exists()) {
      dirtyModulesRegistry.delete();
    }

    MavenModule wsArtifact1 = new MavenModule(pom1, "at.nonblocking", "test3", "1.0.0-1222");
    List<MavenModule> artifactList = new ArrayList<>();
    artifactList.add(wsArtifact1);

    MavenProject mavenProject = new MavenProject();
    mavenProject.setFile(new File("target"));

    this.nonSnapshotMojo.setDeferPomCommit(true);
    when(this.mockModuleTraverser.findAllModules(mavenProject, Collections.<Profile>emptyList())).thenReturn(Arrays.asList(model1));
    when(this.mockMavenPomHandler.readArtifact(model1)).thenReturn(wsArtifact1);

    //when(this.mockScmHandler.getRevisionId(pom1.getParentFile())).thenReturn("1333");
    when(this.mockScmHandler.isWorkingCopy(any(File.class))).thenReturn(true);

    this.nonSnapshotMojo.execute();

    assertTrue(dirtyModulesRegistry.exists());

    BufferedReader reader = new BufferedReader(new FileReader(dirtyModulesRegistry));
    assertNotNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

}
