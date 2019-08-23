package at.nonblocking.maven.nonsnapshot;

public class ResourcesToolkit {

  public static String GetPathToResourceInTarget(String resource)
  {
    return "target/" + resource;
  }

  public static String GetPathToResourceInResourcesDir(Class c, String resource)
  {
    return c.getClassLoader().getResource(resource).getPath();
  }

}
