package at.nonblocking.maven.nonsnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse version of a module.
 *
 * @author Yablokov Aleksey
 */
class VersionParser {
    private final Pattern pattern;

    VersionParser(String incrementVersionPattern) {
        pattern = Pattern.compile(incrementVersionPattern);
    }

    Version parse(String version) {
        Matcher m = pattern.matcher(version);
        if (m.find()) {
            String build = m.group(6);
            return new Version(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    m.group(5),
                    build != null ? Integer.parseInt(build) : null
            );
        } else {
            throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version);
        }
    }

    static class Version {
        private final int majorVersion;
        private final int middleVersion;
        private final int minorVersion;
        private final String branchSuffix;
        private final Integer buildVersion;

        Version(int majorVersion, int middleVersion, int minorVersion, String branchSuffix, Integer buildVersion) {
            this.majorVersion = majorVersion;
            this.middleVersion = middleVersion;
            this.minorVersion = minorVersion;
            this.branchSuffix = branchSuffix;
            this.buildVersion = buildVersion;
            if (branchSuffix != null && branchSuffix.isEmpty()) {
                throw new IllegalArgumentException("Branch suffix is an empty string");
            }
            if (branchSuffix != null && buildVersion == null) {
                throw new IllegalArgumentException("Branch suffix is not null, but build version is null");
            }
            if (branchSuffix == null && buildVersion != null) {
                throw new IllegalArgumentException("Build version is not null, but branch suffix is null");
            }
        }

        public int getMajorVersion() {
            return majorVersion;
        }

        public int getMiddleVersion() {
            return middleVersion;
        }

        public int getMinorVersion() {
            return minorVersion;
        }

        public String getBranchSuffix() {
            return branchSuffix;
        }

        public Integer getBuildVersion() {
            return buildVersion;
        }
    }

}
