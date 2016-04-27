package at.nonblocking.maven.nonsnapshot.version;

/**
 * @author Yablokov Aleksey
 */
class VersionIncrementer {

    static VersionParser.Version incrementMinorVersion(VersionParser.Version version) {
        return new VersionParser.Version(
                version.getMajorVersion(),
                version.getMiddleVersion(),
                version.getMinorVersion() + 1,
                version.getBranchSuffix(),
                version.getBuildVersion()
        );
    }

    static VersionParser.Version removeBranchName(VersionParser.Version version) {
        return new VersionParser.Version(
                version.getMajorVersion(),
                version.getMiddleVersion(),
                version.getMinorVersion(),
                null,
                null
        );
    }

    static VersionParser.Version incrementBuildVersion(VersionParser.Version version, String branchName) {
        if (version.getBuildVersion() != null) {
            return new VersionParser.Version(
                    version.getMajorVersion(),
                    version.getMiddleVersion(),
                    version.getMinorVersion(),
                    branchName,
                    version.getBuildVersion() + 1
            );
        } else {
            return new VersionParser.Version(
                    version.getMajorVersion(),
                    version.getMiddleVersion(),
                    version.getMinorVersion(),
                    branchName,
                    1
            );
        }
    }

}
