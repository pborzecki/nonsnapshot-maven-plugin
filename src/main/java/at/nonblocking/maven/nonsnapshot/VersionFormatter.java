package at.nonblocking.maven.nonsnapshot;

/**
 * @author Yablokov Aleksey
 */
class VersionFormatter {

    static String formatWithBranch(VersionParser.Version version) {
        StringBuilder sb = new StringBuilder();
        sb.append(version.getMajorVersion()).append(".");
        sb.append(version.getMiddleVersion()).append(".");
        sb.append(version.getMinorVersion());
        if (version.getBranchSuffix() != null && version.getBuildVersion() != null) {
            sb.append("-");
            sb.append(version.getBranchSuffix()).append("-");
            sb.append(version.getBuildVersion());
        }
        return sb.toString();
    }

    static String formatWithoutBranch(VersionParser.Version version) {
        StringBuilder sb = new StringBuilder();
        sb.append(version.getMajorVersion()).append(".");
        sb.append(version.getMiddleVersion()).append(".");
        sb.append(version.getMinorVersion());
        return sb.toString();
    }
}
