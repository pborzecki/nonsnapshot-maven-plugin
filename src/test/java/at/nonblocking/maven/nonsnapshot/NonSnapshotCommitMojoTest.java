package at.nonblocking.maven.nonsnapshot;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;

import at.nonblocking.maven.nonsnapshot.model.MavenModule;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.impl.StaticLoggerBinder;

public class NonSnapshotCommitMojoTest {

  private NonSnapshotCommitMojo nonSnapshotMojo = new NonSnapshotCommitMojo();
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

    this.nonSnapshotMojo.setScmUser("foo");
    this.nonSnapshotMojo.setScmPassword("bar");
    this.nonSnapshotMojo.setDeferPomCommit(false);
    this.nonSnapshotMojo.setModuleTraverser(this.mockModuleTraverser);
    this.nonSnapshotMojo.setDependencyTreeProcessor(this.mockDependencyTreeProcessor);
    this.nonSnapshotMojo.setMavenPomHandler(this.mockMavenPomHandler);
    this.nonSnapshotMojo.setScmHandler(this.mockScmHandler);
    this.nonSnapshotMojo.setUpstreamDependencyHandler(this.mockUpstreamDependencyHandler);
  }

  @Test
  public void testCommit() throws Exception {
    File pomFilesToCommit = new File("target/nonSnapshotDirtyModules.txt");
    File pom1 = new File("target/pom.xml").getAbsoluteFile();
    File pom2 = new File("target/test1/pom.xml").getAbsoluteFile();
    File pom3 = new File("target/test2/pom.xml").getAbsoluteFile();
    File pom4 = new File("test3/pom.xml").getAbsoluteFile();

    PrintWriter writer = new PrintWriter(pomFilesToCommit);
    writer.write("." + System.getProperty("line.separator"));
    writer.write("test1" + System.getProperty("line.separator"));
    writer.write("test2" + System.getProperty("line.separator"));
    writer.write("../test3" + System.getProperty("line.separator"));
    writer.close();

    when(this.mockMavenPomHandler.readArtifact(pom1)).thenReturn(new MavenModule(pom1, "at.nonblocking.tests", "test-all", "1.2.3"));
    when(this.mockMavenPomHandler.readArtifact(pom2)).thenReturn(new MavenModule(pom1, "at.nonblocking.tests", "test1", "1.1.1"));
    when(this.mockMavenPomHandler.readArtifact(pom3)).thenReturn(new MavenModule(pom3, "at.nonblocking.tests", "test2", "2.2.2"));
    when(this.mockMavenPomHandler.readArtifact(pom4)).thenReturn(new MavenModule(pom4, "at.nonblocking", "test3", "3.3.3"));

    this.nonSnapshotMojo.execute();

    StringBuilder expectedMessageBuilder = new StringBuilder();
    expectedMessageBuilder.append(ScmHandler.NONSNAPSHOT_COMMIT_MESSAGE_PREFIX).append(" Version of 4 artifacts updated\n\nChanges:\n");
    expectedMessageBuilder.append(" - ").append("test-all").append("-").append("1.2.3").append("\n");
    expectedMessageBuilder.append(" - ").append("test1").append("-").append("1.1.1").append("\n");
    expectedMessageBuilder.append(" - ").append("test2").append("-").append("2.2.2").append("\n");
    expectedMessageBuilder.append(" - ").append("test3").append("-").append("3.3.3").append("\n");
    verify(this.mockScmHandler).commitFiles(Arrays.asList(pom1, pom2, pom3, pom4), expectedMessageBuilder.toString());

    assertFalse(pomFilesToCommit.exists());
  }

  @Test
  public void testCommitIgnoreDuplicateEntries() throws Exception {
    File pomFilesToCommit = new File("target/nonSnapshotDirtyModules.txt");
    File pom1 = new File("target/test1/pom.xml").getAbsoluteFile();
    File pom2 = new File("target/test2/pom.xml").getAbsoluteFile();
    File pom3 = new File("target/test3/pom.xml").getAbsoluteFile();

    PrintWriter writer = new PrintWriter(pomFilesToCommit);
    writer.write("test1" + System.getProperty("line.separator"));
    writer.write("test2" + System.getProperty("line.separator"));
    writer.write("test3" + System.getProperty("line.separator"));
    writer.write("test2" + System.getProperty("line.separator"));
    writer.close();

    when(this.mockMavenPomHandler.readArtifact(pom1)).thenReturn(new MavenModule(pom1, "at.nonblocking.tests", "test1", "1.1.1"));
    when(this.mockMavenPomHandler.readArtifact(pom2)).thenReturn(new MavenModule(pom2, "at.nonblocking.tests", "test2", "2.2.2"));
    when(this.mockMavenPomHandler.readArtifact(pom3)).thenReturn(new MavenModule(pom3, "at.nonblocking.tests", "test3", "3.3.3"));

    this.nonSnapshotMojo.execute();

    StringBuilder expectedMessageBuilder = new StringBuilder();
    expectedMessageBuilder.append(ScmHandler.NONSNAPSHOT_COMMIT_MESSAGE_PREFIX).append(" Version of 3 artifacts updated\n\nChanges:\n");
    expectedMessageBuilder.append(" - ").append("test1").append("-").append("1.1.1").append("\n");
    expectedMessageBuilder.append(" - ").append("test2").append("-").append("2.2.2").append("\n");
    expectedMessageBuilder.append(" - ").append("test3").append("-").append("3.3.3").append("\n");
    verify(this.mockScmHandler).commitFiles(Arrays.asList(pom1, pom2, pom3), expectedMessageBuilder.toString());

    assertFalse(pomFilesToCommit.exists());
  }

  @Test
  public void testDontFailOnCommitTrue() throws Exception {
    File pomFilesToCommit = new File("target/nonSnapshotDirtyModules.txt");
    File pom1 = new File("target/test1/pom.xml").getAbsoluteFile();

    PrintWriter writer = new PrintWriter(pomFilesToCommit);
    writer.write("test1" + System.getProperty("line.separator"));
    writer.close();

    when(this.mockMavenPomHandler.readArtifact(pom1)).thenReturn(new MavenModule(pom1, "at.nonblocking.tests", "test1", "1.1.1"));

    StringBuilder expectedMessageBuilder = new StringBuilder();
    expectedMessageBuilder.append(ScmHandler.NONSNAPSHOT_COMMIT_MESSAGE_PREFIX).append(" Version of 1 artifacts updated\n\nChanges:\n");
    expectedMessageBuilder.append(" - ").append("test1").append("-").append("1.1.1").append("\n");
    doThrow(new RuntimeException("test")).when(this.mockScmHandler).commitFiles(anyList(), eq(expectedMessageBuilder.toString()));

    this.nonSnapshotMojo.setDontFailOnCommit(true);
    this.nonSnapshotMojo.execute();
  }

  @Test(expected = RuntimeException.class)
  public void testDontFailOnCommitFalse() throws Exception {
    File pomFilesToCommit = new File("target/nonSnapshotDirtyModules.txt");
    File pom1 = new File("target/test1/pom.xml").getAbsoluteFile();

    PrintWriter writer = new PrintWriter(pomFilesToCommit);
    writer.write("test1" + System.getProperty("line.separator"));
    writer.close();

    when(this.mockMavenPomHandler.readArtifact(pom1)).thenReturn(new MavenModule(pom1, "at.nonblocking.tests", "test1", "1.1.1"));

    StringBuilder expectedMessageBuilder = new StringBuilder();
    expectedMessageBuilder.append(ScmHandler.NONSNAPSHOT_COMMIT_MESSAGE_PREFIX).append(" Version of 1 artifacts updated\n\nChanges:\n");
    expectedMessageBuilder.append(" - ").append("test1").append("-").append("1.1.1").append("\n");
    doThrow(new RuntimeException("test")).when(this.mockScmHandler).commitFiles(anyList(), eq(expectedMessageBuilder.toString()));

    this.nonSnapshotMojo.setDontFailOnCommit(false);
    this.nonSnapshotMojo.execute();
  }

}
