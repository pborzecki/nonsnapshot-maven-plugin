package at.nonblocking.maven.nonsnapshot;

import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotPluginException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Form string with new version of module.
 *
 * @author Yablokov Aleksey
 */
class NewVersionResolver {
    private final boolean appendBranchNameToVersion;
    private final String incrementVersionPattern;
    private final String replaceSpecialSymbolsInVersionBy;

    NewVersionResolver(boolean appendBranchNameToVersion, String incrementVersionPattern, String replaceSpecialSymbolsInVersionBy) {
        this.appendBranchNameToVersion = appendBranchNameToVersion;
        this.incrementVersionPattern = incrementVersionPattern;
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
        if (appendBranchNameToVersion) {
            Pattern pattern = Pattern.compile("(.+)-" + Pattern.quote(branchName) + "-(\\d+)");
            Matcher m = pattern.matcher(currVersion);
            if (m.matches()) {
                String next = Integer.toString(Integer.parseInt(m.group(2)) + 1);
                newVersion = m.group(1) + "-" + branchName + "-" + next;
            } else {
                newVersion = currVersion + "-" + branchName + "-1";
            }
            newVersion = replaceSpecialSymbols(newVersion);
        } else {
            Pattern pattern = Pattern.compile(incrementVersionPattern);
            Matcher m = pattern.matcher(currVersion);
            if (m.matches()) {
                String next = Integer.toString(Integer.parseInt(m.group(1)) + 1);
                newVersion = new StringBuilder(currVersion).replace(m.start(1), m.end(1), next).toString();
            } else {
                throw new NonSnapshotPluginException("Unsupported version format " + currVersion);
            }
        }
        return newVersion;
    }

    private String replaceSpecialSymbols(String newVersion) {
        newVersion = newVersion.replaceAll("/", replaceSpecialSymbolsInVersionBy);
        return newVersion;
    }
}
