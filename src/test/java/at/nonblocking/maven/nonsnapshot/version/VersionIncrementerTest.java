package at.nonblocking.maven.nonsnapshot.version;

import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.version.VersionIncrementer.incrementBuildVersion;
import static at.nonblocking.maven.nonsnapshot.version.VersionIncrementer.incrementMinorVersion;

/**
 * @author Yablokov Aleksey
 */
public class VersionIncrementerTest {
    private static final VersionParser.Version noBranch = new VersionParser.Version(1, 2, 3, null, null);
    private static final VersionParser.Version withBranch = new VersionParser.Version(1, 2, 3, "master", 4);

    @Test
    public void incrementMinorVersionTest() {
        VersionParserTest.assertVersionEquals(incrementMinorVersion(noBranch), 1, 2, 4, null, null);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(withBranch), 1, 2, 4, "master", 4);
    }

    @Test
    public void incrementBuildVersionTest() {
        VersionParserTest.assertVersionEquals(incrementBuildVersion(noBranch, "master"), 1, 2, 3, "master", 1);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(withBranch, "master"), 1, 2, 3, "master", 5);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(withBranch, "gvc-3"), 1, 2, 3, "gvc-3", 5);
    }

}