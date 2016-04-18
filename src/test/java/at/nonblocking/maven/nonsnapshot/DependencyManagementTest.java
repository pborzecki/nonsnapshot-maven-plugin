package at.nonblocking.maven.nonsnapshot;

import at.nonblocking.maven.nonsnapshot.impl.DependencyTreeProcessorDefaultImpl;
import at.nonblocking.maven.nonsnapshot.model.MavenArtifact;
import at.nonblocking.maven.nonsnapshot.model.MavenModule;
import at.nonblocking.maven.nonsnapshot.model.MavenModuleDependency;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DependencyManagementTest {

  @BeforeClass
  public static void setupLog() {
    StaticLoggerBinder.getSingleton().setLog(new DebugSystemStreamLog());
  }

  @Test
  public void testBuildDependencyTree() {
    File root = new File(getClass().getClassLoader().getResource("dependency_management/projectA/pom.xml").getFile());
    MavenModule wsArtifact1 = new MavenModule(root, "at.nonblocking", "root", "1.0.0");
    MavenModule wsArtifact2 = new MavenModule(null, "at.nonblocking", "parent", "2.0.0");
    MavenModule wsArtifact3 = new MavenModule(null, "at.nonblocking", "child1", "3.1.0");
    MavenModule wsArtifact4 = new MavenModule(null, "at.nonblocking", "child2", "3.2.0");

    wsArtifact1.getDependencies().add(new MavenModuleDependency(0, new MavenArtifact("at.nonblocking.at", "plugin1", "1.0.0")));
    wsArtifact1.getDependencies().add(new MavenModuleDependency(0, new MavenArtifact("junit", "junit", "4.7")));
    wsArtifact2.setParent(new MavenArtifact("at.nonblocking.at", "parent", "1.0.0"));
    wsArtifact2.getDependencies().add(new MavenModuleDependency(0, new MavenArtifact("at.nonblocking.at", "test2", "1.0.0")));
    wsArtifact3.setParent(new MavenArtifact("at.nonblocking.at", "test1", "1.0.0"));
    wsArtifact4.setParent(new MavenArtifact("at.nonblocking.at", "test1", "1.0.0"));

    List<MavenModule> artifacts = new ArrayList<MavenModule>();
    artifacts.add(wsArtifact1);
    artifacts.add(wsArtifact2);
    artifacts.add(wsArtifact3);
    artifacts.add(wsArtifact4);

    DependencyTreeProcessor dependencyTreeProcessor = new DependencyTreeProcessorDefaultImpl();

    dependencyTreeProcessor.buildDependencyTree(artifacts);

    dependencyTreeProcessor.printMavenModuleTree(wsArtifact1, System.out);
  }


}
