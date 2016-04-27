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
        assertVersionEquals(parser.parse("1.2.3"), 1, 2, 3, null, null);
        assertVersionEquals(parser.parse("11.22.33"), 11, 22, 33, null, null);
    }

    @Test
    public void withBranch() {
        assertVersionEquals(parser.parse("1.2.3-master-4"), 1, 2, 3, "master", 4);
        assertVersionEquals(parser.parse("1.2.3-gvc-4-5"), 1, 2, 3, "gvc-4", 5);
        assertVersionEquals(parser.parse("1.2.3-release-gvc-4-5"), 1, 2, 3, "release-gvc-4", 5);
        assertVersionEquals(parser.parse("1.2.3-pks-1234-bnu-integration-5"), 1, 2, 3, "pks-1234-bnu-integration", 5);
    }

    static void assertVersionEquals(VersionParser.Version actual, int major, int middle, int minor, String branch, Integer build) {
        assertThat(actual.getMajorVersion(), equalTo(major));
        assertThat(actual.getMiddleVersion(), equalTo(middle));
        assertThat(actual.getMinorVersion(), equalTo(minor));
        assertThat(actual.getBranchSuffix(), equalTo(branch));
        assertThat(actual.getBuildVersion(), equalTo(build));
    }

}