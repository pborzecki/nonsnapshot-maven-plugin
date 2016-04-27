package at.nonblocking.maven.nonsnapshot;

/**
 * Form string with new version of module.
 *
 * @author Yablokov Aleksey
 */
class NewVersionResolver {
    private final boolean appendBranchNameToVersion;
    private final String replaceSpecialSymbolsInVersionBy;
    private final VersionParser parser;

    NewVersionResolver(boolean appendBranchNameToVersion, String incrementVersionPattern, String replaceSpecialSymbolsInVersionBy) {
        this.appendBranchNameToVersion = appendBranchNameToVersion;
        this.parser = new VersionParser(incrementVersionPattern);
        this.replaceSpecialSymbolsInVersionBy = replaceSpecialSymbolsInVersionBy;
    }

    String resolveNewVersion(String currVersion, String branchName) {
        if (currVersion == null) {
            throw new IllegalArgumentException("Current version is null");
        }
        if (branchName == null || branchName.isEmpty()) {
            throw new IllegalArgumentException("Branch name is null");
        }

        String newVersion;
        VersionParser.Version currV = parser.parse(currVersion);
        if (appendBranchNameToVersion) {
            VersionParser.Version newV = VersionIncrementer.incrementBuildVersion(currV, branchName);
            newVersion = VersionFormatter.formatWithBranch(newV);
            newVersion = replaceSpecialSymbols(newVersion);
        } else {
            VersionParser.Version newV = VersionIncrementer.incrementMinorVersion(currV);
            newVersion = VersionFormatter.formatWithBranch(newV);
        }
        return newVersion;
    }

    private String replaceSpecialSymbols(String newVersion) {
        newVersion = newVersion.replaceAll("/", replaceSpecialSymbolsInVersionBy);
        return newVersion;
    }
}
