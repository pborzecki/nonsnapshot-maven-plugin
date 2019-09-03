package at.nonblocking.maven.nonsnapshot.version;

import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.Constants.DEFAULT_INCREMENT_VERSION_PATTERN;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yablokov Aleksey
 */
public class VersionParserTest {

    private static final VersionParser parser = new VersionParser(DEFAULT_INCREMENT_VERSION_PATTERN);

    @Test
    public void noBranch() {
        assertVersionEquals(parser.parse("1.2.3"), 1, 2, 3, null, null, false);
        assertVersionEquals(parser.parse("11.22.33"), 11, 22, 33, null, null, false);
        assertVersionEquals(parser.parse("11.22.33-SNAPSHOT"), 11, 22, 33, null, null, true);
    }

    @Test
    public void withBranch() {
        assertVersionEquals(parser.parse("1.2.3-master-4"), 1, 2, 3, "master", 4, false);
        assertVersionEquals(parser.parse("1.2.3-master-SNAPSHOT"), 1, 2, 3, "master", null, true);
        assertVersionEquals(parser.parse("1.2.3-gvc-4-5"), 1, 2, 3, "gvc-4", 5, false);
        assertVersionEquals(parser.parse("1.2.3-gvc-4-SNAPSHOT"), 1, 2, 3, "gvc-4", null, true);
        assertVersionEquals(parser.parse("1.2.3-release-gvc-4-5"), 1, 2, 3, "release-gvc-4", 5, false);
        assertVersionEquals(parser.parse("1.2.3-release-gvc-4-SNAPSHOT"), 1, 2, 3, "release-gvc-4", null, true);
        assertVersionEquals(parser.parse("1.2.3-pks-1234-bnu-integration-5"), 1, 2, 3, "pks-1234-bnu-integration", 5, false);
        assertVersionEquals(parser.parse("1.2.3-pks-1234-bnu-integration-SNAPSHOT"), 1, 2, 3, "pks-1234-bnu-integration", null, true);
    }

    static void assertVersionEquals(VersionParser.Version actual, int major, int middle, int minor, String branch, Integer build, boolean isSnapshot) {
        assertThat(actual.getMajorVersion(), equalTo(major));
        assertThat(actual.getMiddleVersion(), equalTo(middle));
        assertThat(actual.getMinorVersion(), equalTo(minor));
        assertThat(actual.getBranchSuffix(), equalTo(branch));
        assertThat(actual.getBuildVersion(), equalTo(build));
        assertThat(actual.getIsItSnapshot(), equalTo(isSnapshot));
    }

}