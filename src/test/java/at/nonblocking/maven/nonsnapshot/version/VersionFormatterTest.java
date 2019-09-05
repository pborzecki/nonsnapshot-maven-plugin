package at.nonblocking.maven.nonsnapshot.version;

import at.nonblocking.maven.nonsnapshot.version.VersionParser;
import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.version.VersionFormatter.formatWithBranch;
import static at.nonblocking.maven.nonsnapshot.version.VersionFormatter.formatWithoutBranch;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yablokov Aleksey
 */
public class VersionFormatterTest {
    private static final Version noBranch = new Version(1, 2, 3, null, null, false);
    private static final Version noBranchSnapshot = new Version(1, 2, 3, null, null, true);
    private static final Version easyBranch = new Version(1, 2, 3, "master", 4, false);
    private static final Version easyBranchSnapshot = new Version(1, 2, 3, "master", null, true);
    private static final Version hardBranch = new Version(1, 2, 3, "pks-1234-bnu-integration-5", 4, false);
    private static final Version hardBranchSnapshot = new Version(1, 2, 3, "pks-1234-bnu-integration-5", null, true);

    @Test
    public void withBranch() {
        assertThat(formatWithBranch(noBranch), equalTo("1.2.3"));
        assertThat(formatWithBranch(noBranchSnapshot), equalTo("1.2.3-SNAPSHOT"));
        assertThat(formatWithBranch(easyBranch), equalTo("1.2.3-master-4"));
        assertThat(formatWithBranch(easyBranchSnapshot), equalTo("1.2.3-master-SNAPSHOT"));
        assertThat(formatWithBranch(hardBranch), equalTo("1.2.3-pks-1234-bnu-integration-5-4"));
        assertThat(formatWithBranch(hardBranchSnapshot), equalTo("1.2.3-pks-1234-bnu-integration-5-SNAPSHOT"));
    }

    @Test
    public void withoutBranch() {
        assertThat(formatWithoutBranch(noBranch), equalTo("1.2.3"));
        assertThat(formatWithoutBranch(noBranchSnapshot), equalTo("1.2.3-SNAPSHOT"));
        assertThat(formatWithoutBranch(easyBranch), equalTo("1.2.3"));
        assertThat(formatWithoutBranch(easyBranchSnapshot), equalTo("1.2.3-SNAPSHOT"));
        assertThat(formatWithoutBranch(hardBranch), equalTo("1.2.3"));
        assertThat(formatWithoutBranch(hardBranchSnapshot), equalTo("1.2.3-SNAPSHOT"));
    }

}