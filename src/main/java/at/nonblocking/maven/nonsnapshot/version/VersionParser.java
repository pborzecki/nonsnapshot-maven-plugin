package at.nonblocking.maven.nonsnapshot.version;

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
        final String SNAPSHOT = "SNAPSHOT";

        Matcher m = pattern.matcher(version);
        if (m.find()) {
            String buildNumber = m.group(6);
            Integer buildVersion = null;
            boolean isItSnapshot = false;

            if(m.group(7) != null) {
                if(m.group(7).equals(SNAPSHOT))
                    isItSnapshot = true;
                else
                    throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version);
            }

            if(buildNumber != null) {
                if(buildNumber.equals(SNAPSHOT))
                    isItSnapshot = true;
                else
                    buildVersion = Integer.parseInt(buildNumber);
            } else if(!isItSnapshot && m.group(5) != null)
                buildVersion = 1;

            return new Version(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    m.group(5),
                    buildVersion,
                    isItSnapshot
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
        private final boolean isItSnapshot;

        Version(int majorVersion, int middleVersion, int minorVersion, String branchSuffix, Integer buildVersion, boolean isItSnapshot) {
            this.majorVersion = majorVersion;
            this.middleVersion = middleVersion;
            this.minorVersion = minorVersion;
            this.branchSuffix = branchSuffix;
            this.buildVersion = buildVersion;
            this.isItSnapshot = isItSnapshot;

            if (branchSuffix != null && branchSuffix.isEmpty()) {
                throw new IllegalArgumentException("Branch suffix is an empty string");
            }
            if (branchSuffix != null && buildVersion == null && !isItSnapshot) {
                throw new IllegalArgumentException("Branch suffix is not null, but it is not SNAPSHOT and build version is null");
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

        public Integer getBuildVersion() { return buildVersion; }

        public boolean getIsItSnapshot() {
            return isItSnapshot;
        }
    }

}
