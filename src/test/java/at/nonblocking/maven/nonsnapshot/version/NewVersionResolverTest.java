package at.nonblocking.maven.nonsnapshot.version;

import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.Constants.DEFAULT_INCREMENT_VERSION_PATTERN;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yablokov Aleksey
 */
public class NewVersionResolverTest {
    private static final NewVersionResolver resolver = new NewVersionResolver(true, DEFAULT_INCREMENT_VERSION_PATTERN, null);

    @Test
    public void noAppendBranchNameToVersion() {
        NewVersionResolver resolver = new NewVersionResolver(false, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3", "master"), equalTo("1.2.4"));
    }

    @Test
    public void appendBranchNameToVersion() {
        assertThat(resolver.resolveNewVersion("1.2.3", "master"), equalTo("1.2.3-master-1"));
    }

    @Test
    public void replaceSuffix() {
        assertThat(resolver.resolveNewVersion("1.2.3-pks-10", "master"), equalTo("1.2.3-master-11"));
    }
}