package at.nonblocking.maven.nonsnapshot.version;

import org.junit.Test;

import static at.nonblocking.maven.nonsnapshot.Constants.DEFAULT_INCREMENT_VERSION_PATTERN;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yablokov Aleksey
 */
public class NewVersionResolverTest {
    @Test
    public void noAppendBranchNameToVersion() {
        NewVersionResolver resolver = new NewVersionResolver(false, false, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3", "master"), equalTo("1.2.4"));

        resolver = new NewVersionResolver(false, true, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3", "master"), equalTo("1.2.4-SNAPSHOT"));
    }

    @Test
    public void appendBranchNameToVersion() {
        NewVersionResolver resolver = new NewVersionResolver(true, false, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3", "master"), equalTo("1.2.3-master-1"));

        resolver = new NewVersionResolver(true, true, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3", "master"), equalTo("1.2.3-master-SNAPSHOT"));
    }

    @Test
    public void replaceSuffix() {
        NewVersionResolver resolver = new NewVersionResolver(true, false, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3-pks-10", "master"), equalTo("1.2.3-master-11"));

        resolver = new NewVersionResolver(true, true, DEFAULT_INCREMENT_VERSION_PATTERN, null);
        assertThat(resolver.resolveNewVersion("1.2.3-pks-10", "master"), equalTo("1.2.3-master-SNAPSHOT"));
    }
}