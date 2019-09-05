package at.nonblocking.maven.nonsnapshot.version;

/**
 * @author Yablokov Aleksey
 */
class VersionIncrementer {

    static Version incrementMinorVersion(Version version, boolean useSnapshot) {
        Integer buildVersion = version.getBuildVersion();
        if(useSnapshot || version.getBranchSuffix() == null)
            buildVersion = null;
        else if(buildVersion == null)
            buildVersion = 1;

        return new Version(
                version.getMajorVersion(),
                version.getMiddleVersion(),
                version.getMinorVersion() + 1,
                version.getBranchSuffix(),
                buildVersion,
                useSnapshot
        );
    }

    static Version removeBranchName(Version version) {
        return new Version(
                version.getMajorVersion(),
                version.getMiddleVersion(),
                version.getMinorVersion(),
                null,
                null,
                version.getIsItSnapshot()
        );
    }

    static Version incrementBuildVersion(Version version, String branchName, boolean useSnapshot) {
        Integer buildVersion =  version.getBuildVersion();
        if(buildVersion != null)
            buildVersion = useSnapshot ? null : buildVersion + 1;
        else
            buildVersion = useSnapshot ? null : 1;

        return new Version(
                version.getMajorVersion(),
                version.getMiddleVersion(),
                version.getMinorVersion(),
                branchName,
                buildVersion,
                useSnapshot
        );
    }

}
