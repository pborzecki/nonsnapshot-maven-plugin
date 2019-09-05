package at.nonblocking.maven.nonsnapshot;

/**
 * @author Yablokov Aleksey
 */
public interface Constants {
    String DEFAULT_INCREMENT_VERSION_PATTERN = "^((\\d+)(\\.(\\d+)){2})(-(\\w[\\w-]*)-(\\d+|SNAPSHOT)|-(SNAPSHOT)|)$?";
    String DEFAULT_UPSTREAM_DEPENDENCY_VERSION_PATTERN = "^((\\d+)(\\.(\\d+)){0,2})(-(\\w[\\w-]*)-(\\d+|SNAPSHOT)|-(SNAPSHOT)|)$|(^LATEST$|^$)?";
}
