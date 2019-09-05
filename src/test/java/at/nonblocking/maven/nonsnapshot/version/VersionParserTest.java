package at.nonblocking.maven.nonsnapshot.version;

import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.Constants.DEFAULT_INCREMENT_VERSION_PATTERN;
import static at.nonblocking.maven.nonsnapshot.Constants.DEFAULT_UPSTREAM_DEPENDENCY_VERSION_PATTERN;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * @author Yablokov Aleksey
 */
public class VersionParserTest {

    private static final VersionParser incrementVersionParser = new VersionParser(DEFAULT_INCREMENT_VERSION_PATTERN);
    private static final VersionParser upstreamDependencyVersionParser = new VersionParser(DEFAULT_UPSTREAM_DEPENDENCY_VERSION_PATTERN);

    @Test
    public void noBranch() {
        VersionParser p = incrementVersionParser;
        assertParsingThrows(p, "");
        assertParsingThrows(p, "LATEST");
        assertParsingThrows(p, "1");
        assertParsingThrows(p, "1-SNAPSHOT");
        assertParsingThrows(p, "1.2");
        assertParsingThrows(p, "1.2-SNAPSHOT");
        assertVersionEquals(p.parse("1.2.3"), 1, 2, 3, null, null, false);
        assertVersionEquals(p.parse("1.2.3-SNAPSHOT"), 1, 2, 3, null, null, true);

        p = upstreamDependencyVersionParser;
        assertVersionEquals(p.parse(""), null, null, null, null, null, false);
        assertVersionEquals(p.parse("LATEST"), null, null, null, null, null, false);
        assertVersionEquals(p.parse("1"), 1, null, null, null, null, false);
        assertVersionEquals(p.parse("1-SNAPSHOT"), 1, null, null, null, null, true);
        assertVersionEquals(p.parse("1.2"), 1, 2, null, null, null, false);
        assertVersionEquals(p.parse("1.2-SNAPSHOT"), 1, 2, null, null, null, true);
        assertVersionEquals(p.parse("1.2.3"), 1, 2, 3, null, null, false);
        assertVersionEquals(p.parse("1.2.3-SNAPSHOT"), 1, 2, 3, null, null, true);
    }

    @Test
    public void withBranch() {
        VersionParser p = incrementVersionParser;
        assertParsingThrows(p, "1-master-4");
        assertParsingThrows(p, "1-master-SNAPSHOT");
        assertParsingThrows(p, "1-gvc-4-5");
        assertParsingThrows(p, "1-gvc-4-SNAPSHOT");
        assertParsingThrows(p, "1-release-gvc-4-5");
        assertParsingThrows(p, "1-release-gvc-4-SNAPSHOT");
        assertParsingThrows(p, "1-pks-1234-bnu-integration-5");
        assertParsingThrows(p, "1-pks-1234-bnu-integration-SNAPSHOT");
        assertParsingThrows(p, "1-master-4");
        assertParsingThrows(p, "1.2-master-4");
        assertParsingThrows(p, "1.2-master-SNAPSHOT");
        assertParsingThrows(p, "1.2-gvc-4-5");
        assertParsingThrows(p, "1.2-gvc-4-SNAPSHOT");
        assertParsingThrows(p, "1.2-release-gvc-4-5");
        assertParsingThrows(p, "1.2-release-gvc-4-SNAPSHOT");
        assertParsingThrows(p, "1.2-pks-1234-bnu-integration-5");
        assertParsingThrows(p, "1.2-pks-1234-bnu-integration-SNAPSHOT");
        assertVersionEquals(p.parse("1.2.3-master-4"), 1, 2, 3, "master", 4, false);
        assertVersionEquals(p.parse("1.2.3-master-SNAPSHOT"), 1, 2, 3, "master", null, true);
        assertVersionEquals(p.parse("1.2.3-gvc-4-5"), 1, 2, 3, "gvc-4", 5, false);
        assertVersionEquals(p.parse("1.2.3-gvc-4-SNAPSHOT"), 1, 2, 3, "gvc-4", null, true);
        assertVersionEquals(p.parse("1.2.3-release-gvc-4-5"), 1, 2, 3, "release-gvc-4", 5, false);
        assertVersionEquals(p.parse("1.2.3-release-gvc-4-SNAPSHOT"), 1, 2, 3, "release-gvc-4", null, true);
        assertVersionEquals(p.parse("1.2.3-pks-1234-bnu-integration-5"), 1, 2, 3, "pks-1234-bnu-integration", 5, false);
        assertVersionEquals(p.parse("1.2.3-pks-1234-bnu-integration-SNAPSHOT"), 1, 2, 3, "pks-1234-bnu-integration", null, true);


        p = upstreamDependencyVersionParser;
        assertVersionEquals(p.parse("1-master-4"), 1, null, null, "master", 4, false);
        assertVersionEquals(p.parse("1-master-SNAPSHOT"), 1, null, null, "master", null, true);
        assertVersionEquals(p.parse("1-gvc-4-5"), 1, null, null, "gvc-4", 5, false);
        assertVersionEquals(p.parse("1-gvc-4-SNAPSHOT"), 1, null, null, "gvc-4", null, true);
        assertVersionEquals(p.parse("1-release-gvc-4-5"), 1, null, null, "release-gvc-4", 5, false);
        assertVersionEquals(p.parse("1-release-gvc-4-SNAPSHOT"), 1, null, null, "release-gvc-4", null, true);
        assertVersionEquals(p.parse("1-pks-1234-bnu-integration-5"), 1, null, null, "pks-1234-bnu-integration", 5, false);
        assertVersionEquals(p.parse("1-pks-1234-bnu-integration-SNAPSHOT"), 1, null, null, "pks-1234-bnu-integration", null, true);
        assertVersionEquals(p.parse("1.2-master-4"), 1, 2, null, "master", 4, false);
        assertVersionEquals(p.parse("1.2-master-SNAPSHOT"), 1, 2, null, "master", null, true);
        assertVersionEquals(p.parse("1.2-gvc-4-5"), 1, 2, null, "gvc-4", 5, false);
        assertVersionEquals(p.parse("1.2-gvc-4-SNAPSHOT"), 1, 2, null, "gvc-4", null, true);
        assertVersionEquals(p.parse("1.2-release-gvc-4-5"), 1, 2, null, "release-gvc-4", 5, false);
        assertVersionEquals(p.parse("1.2-release-gvc-4-SNAPSHOT"), 1, 2, null, "release-gvc-4", null, true);
        assertVersionEquals(p.parse("1.2-pks-1234-bnu-integration-5"), 1, 2, null, "pks-1234-bnu-integration", 5, false);
        assertVersionEquals(p.parse("1.2-pks-1234-bnu-integration-SNAPSHOT"), 1, 2, null, "pks-1234-bnu-integration", null, true);
        assertVersionEquals(p.parse("1.2.3-master-4"), 1, 2, 3, "master", 4, false);
        assertVersionEquals(p.parse("1.2.3-master-SNAPSHOT"), 1, 2, 3, "master", null, true);
        assertVersionEquals(p.parse("1.2.3-gvc-4-5"), 1, 2, 3, "gvc-4", 5, false);
        assertVersionEquals(p.parse("1.2.3-gvc-4-SNAPSHOT"), 1, 2, 3, "gvc-4", null, true);
        assertVersionEquals(p.parse("1.2.3-release-gvc-4-5"), 1, 2, 3, "release-gvc-4", 5, false);
        assertVersionEquals(p.parse("1.2.3-release-gvc-4-SNAPSHOT"), 1, 2, 3, "release-gvc-4", null, true);
        assertVersionEquals(p.parse("1.2.3-pks-1234-bnu-integration-5"), 1, 2, 3, "pks-1234-bnu-integration", 5, false);
        assertVersionEquals(p.parse("1.2.3-pks-1234-bnu-integration-SNAPSHOT"), 1, 2, 3, "pks-1234-bnu-integration", null, true);

    }

    static void assertVersionEquals(Version actual, Integer major, Integer middle, Integer minor, String branch, Integer build, boolean isSnapshot) {
        assertThat(actual.getMajorVersion(), equalTo(major));
        assertThat(actual.getMiddleVersion(), equalTo(middle));
        assertThat(actual.getMinorVersion(), equalTo(minor));
        assertThat(actual.getBranchSuffix(), equalTo(branch));
        assertThat(actual.getBuildVersion(), equalTo(build));
        assertThat(actual.getIsItSnapshot(), equalTo(isSnapshot));
    }

    static void assertParsingThrows(VersionParser parser, String version) {
        try {
            parser.parse(version);
            assertTrue("Parsing succeeded but failure expected", false);
        } catch (IllegalArgumentException e) {

        }
    }

}