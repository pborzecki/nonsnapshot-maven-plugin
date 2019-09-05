package at.nonblocking.maven.nonsnapshot.version;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse version of a module.
 *
 * @author Yablokov Aleksey
 * @modified Piotr Borzęcki
 */
 public class Version {
    private final Integer majorVersion;
    private final Integer middleVersion;
    private final Integer minorVersion;
    private final String branchSuffix;
    private final Integer buildVersion;
    private final boolean isItSnapshot;

    public Version(Integer majorVersion, Integer middleVersion, Integer minorVersion) {
        this.majorVersion = majorVersion;
        this.middleVersion = middleVersion;
        this.minorVersion = minorVersion;
        this.branchSuffix = null;
        this.buildVersion = null;
        this.isItSnapshot = false;
    }

    public Version(Integer majorVersion, Integer middleVersion, Integer minorVersion, String branchSuffix, Integer buildVersion, boolean isItSnapshot) {
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

    public Integer getMajorVersion() {
        return majorVersion;
    }

    public Integer getMiddleVersion() {
        return middleVersion;
    }

    public Integer getMinorVersion() {
        return minorVersion;
    }

    public String getBranchSuffix() {
        return branchSuffix;
    }

    public Integer getBuildVersion() { return buildVersion; }

    public boolean getIsItSnapshot() {
        return isItSnapshot;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMajorVersion());
        if(getMiddleVersion() != null)
            sb.append(".").append(getMiddleVersion());
        if(getMinorVersion() != null)
            sb.append(".").append(getMinorVersion());
        if (getBranchSuffix() != null) {
            if(getBuildVersion() != null) {
                sb.append("-").append(getBranchSuffix()).append("-").append(getBuildVersion());
            } else if(getIsItSnapshot())
                sb.append("-").append(getBranchSuffix()).append("-SNAPSHOT");
        }
        else if(getIsItSnapshot())
            sb.append("-SNAPSHOT");
        return sb.toString();
    }

}
