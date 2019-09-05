package at.nonblocking.maven.nonsnapshot.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse version of a module.
 *
 * @author Yablokov Aleksey
 * @modified Piotr Borzęcki
 */
public class VersionParser {
    private final String SNAPSHOT = "SNAPSHOT";
    private final Pattern pattern;

    public VersionParser(String incrementVersionPattern) {
        pattern = Pattern.compile(incrementVersionPattern);
    }

    public Version parse(String version) {
        if(version == null)
            version = "";

        Matcher m = pattern.matcher(version);
        if (m.find()) {
            if (version.isEmpty() || m.groupCount() == 9 && m.group(9) != null && m.group(9).equals("LATEST"))
                return new Version(null, null, null, null, null, false);


            Integer majorVersion = null;
            Integer middleVersion = null;
            Integer minorVersion = null;
            String[] versionParts = m.group(1).split("\\.");
            if (versionParts.length < 1)
                throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version + " (major version missing)");

            if (versionParts.length > 3)
                throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version + " (too many version elements)");

            try {
                majorVersion = Integer.parseInt(versionParts[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version + " (major version parse error: " + e.getMessage() + ")");
            }

            if (versionParts.length >= 2)
            {
                try {
                    middleVersion = Integer.parseInt(versionParts[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version + " (middle version parse error: " + e.getMessage() + ")");
                }
            }

            if (versionParts.length >= 3)
            {
                try {
                    minorVersion = Integer.parseInt(versionParts[2]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version + " (minor version parse error: " + e.getMessage() + ")");
                }
            }

            String branchName =  m.group(6);
            Integer buildVersion = null;
            boolean isItSnapshot = false;
            String snapshotOrBuild = m.group(7);
            if(snapshotOrBuild != null) {
                if (snapshotOrBuild.equals(SNAPSHOT))
                    isItSnapshot = true;
                else
                    buildVersion = Integer.parseInt(snapshotOrBuild);
            }

            String snapshot = m.group(8);
            if(snapshot != null && snapshot.equals(SNAPSHOT))
                isItSnapshot = true;

            return new Version(majorVersion, middleVersion, minorVersion, branchName, buildVersion, isItSnapshot);
        } else {
            throw new IllegalArgumentException("Can't parse version with '" + pattern + "': " + version);
        }
    }

}
