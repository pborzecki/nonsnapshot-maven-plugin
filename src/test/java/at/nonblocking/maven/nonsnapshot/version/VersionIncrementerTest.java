package at.nonblocking.maven.nonsnapshot.version;

import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.version.VersionIncrementer.incrementBuildVersion;
import static at.nonblocking.maven.nonsnapshot.version.VersionIncrementer.incrementMinorVersion;

/**
 * @author Yablokov Aleksey
 */
public class VersionIncrementerTest {
    private static final VersionParser.Version noBranch = new VersionParser.Version(1, 2, 3, null, null, false);
    private static final VersionParser.Version noBranchSnapshot = new VersionParser.Version(1, 2, 3, null, null, true);
    private static final VersionParser.Version withBranch = new VersionParser.Version(1, 2, 3, "master", 4, false);
    private static final VersionParser.Version withBranchSnapshot = new VersionParser.Version(1, 2, 3, "master", null, true);

    @Test
    public void incrementMinorVersionTest() {
        VersionParserTest.assertVersionEquals(incrementMinorVersion(noBranch, false), 1, 2, 4, null, null, false);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(noBranch, true), 1, 2, 4, null, null, true);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(noBranchSnapshot, false), 1, 2, 4, null, null, false);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(noBranchSnapshot, true), 1, 2, 4, null, null, true);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(withBranch, false), 1, 2, 4, "master", 4, false);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(withBranch, true), 1, 2, 4, "master", null, true);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(withBranchSnapshot, false), 1, 2, 4, "master", 1, false);
        VersionParserTest.assertVersionEquals(incrementMinorVersion(withBranchSnapshot, true), 1, 2, 4, "master", null, true);
    }

    @Test
    public void incrementBuildVersionTest() {
        VersionParserTest.assertVersionEquals(incrementBuildVersion(noBranch, "master", false), 1, 2, 3, "master", 1, false);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(noBranch, "master", true), 1, 2, 3, "master", null, true);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(noBranchSnapshot, "master", false), 1, 2, 3, "master", 1, false);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(noBranchSnapshot, "master", true), 1, 2, 3, "master", null, true);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(withBranch, "master", false), 1, 2, 3, "master", 5, false);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(withBranch, "master", true), 1, 2, 3, "master", null, true);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(withBranch, "gvc-3", false), 1, 2, 3, "gvc-3", 5, false);
        VersionParserTest.assertVersionEquals(incrementBuildVersion(withBranch, "gvc-3", true), 1, 2, 3, "gvc-3", null, true);
    }

}