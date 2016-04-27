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
    private static final String NONSNAPSHOT_CURRENT_BRANCH = "NONSNAPSHOT_CURRENT_BRANCH";
    private final boolean appendBranchNameToVersion;
    private final String branchName;
    private final ScmHandler scmHandler;
    private final String incrementVersionPattern;
    private final String replaceSpecialSymbolsInVersionBy;

    NewVersionResolver(boolean appendBranchNameToVersion, String branchName, ScmHandler scmHandler, String incrementVersionPattern, String replaceSpecialSymbolsInVersionBy) {
        this.appendBranchNameToVersion = appendBranchNameToVersion;
        this.branchName = branchName;
        this.scmHandler = scmHandler;
        this.incrementVersionPattern = incrementVersionPattern;
        this.replaceSpecialSymbolsInVersionBy = replaceSpecialSymbolsInVersionBy;
    }

    String resolveNewVersion(String currVersion) {
        String newVersion;
        String branch = null;
        if (appendBranchNameToVersion) {
            branch = System.getenv(NONSNAPSHOT_CURRENT_BRANCH);
            if (branch == null) {
                branch = branchName;
            }
            if (branch == null || branch.isEmpty()) {
                branch = scmHandler.getBranchName();
            }
        }
        if (branch != null) {
            Pattern pattern = Pattern.compile("(.+)-" + Pattern.quote(branch) + "-(\\d+)");
            Matcher m = pattern.matcher(currVersion);
            if (m.matches()) {
                String next = Integer.toString(Integer.parseInt(m.group(2)) + 1);
                newVersion = m.group(1) + "-" + branch + "-" + next;
            } else {
                newVersion = currVersion + "-" + branch + "-1";
            }
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
        newVersion = newVersion.replaceAll("/", replaceSpecialSymbolsInVersionBy);
        return newVersion;
    }
}
