package at.nonblocking.maven.nonsnapshot.version;

/**
 * @author Yablokov Aleksey
 */
class VersionFormatter {

    static String formatWithBranch(VersionParser.Version version) {
        StringBuilder sb = new StringBuilder();
        sb.append(version.getMajorVersion()).append(".");
        sb.append(version.getMiddleVersion()).append(".");
        sb.append(version.getMinorVersion());
        if (version.getBranchSuffix() != null) {
            if(version.getBuildVersion() != null) {
                sb.append("-").append(version.getBranchSuffix()).append("-").append(version.getBuildVersion());
            } else if(version.getIsItSnapshot())
                sb.append("-").append(version.getBranchSuffix()).append("-SNAPSHOT");
        }
        else if(version.getIsItSnapshot())
            sb.append("-SNAPSHOT");
        return sb.toString();
    }

    static String formatWithoutBranch(VersionParser.Version version) {
        StringBuilder sb = new StringBuilder();
        sb.append(version.getMajorVersion()).append(".");
        sb.append(version.getMiddleVersion()).append(".");
        sb.append(version.getMinorVersion());
        if(version.getIsItSnapshot())
            sb.append("-SNAPSHOT");
        return sb.toString();
    }
}
