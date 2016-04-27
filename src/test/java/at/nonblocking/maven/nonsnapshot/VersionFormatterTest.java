package at.nonblocking.maven.nonsnapshot;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yablokov Aleksey
 */
public class VersionFormatterTest {
    private static final VersionParser.Version noBranch = new VersionParser.Version(1, 2, 3, null, null);
    private static final VersionParser.Version easyBranch = new VersionParser.Version(1, 2, 3, "master", 4);
    private static final VersionParser.Version hardBranch = new VersionParser.Version(1, 2, 3, "pks-1234-bnu-integration-5", 4);

    @Test
    public void withBranch() {
        assertThat(VersionFormatter.formatWithBranch(noBranch), equalTo("1.2.3"));
        assertThat(VersionFormatter.formatWithBranch(easyBranch), equalTo("1.2.3-master-4"));
        assertThat(VersionFormatter.formatWithBranch(hardBranch), equalTo("1.2.3-pks-1234-bnu-integration-5-4"));
    }

    @Test
    public void withoutBranch() {
        assertThat(VersionFormatter.formatWithoutBranch(noBranch), equalTo("1.2.3"));
        assertThat(VersionFormatter.formatWithoutBranch(easyBranch), equalTo("1.2.3"));
        assertThat(VersionFormatter.formatWithoutBranch(hardBranch), equalTo("1.2.3"));
    }

}