package at.nonblocking.maven.nonsnapshot;


public class BomDependency {
    private String upstreamDependency;

    public void setUpstreamDependency(String upstreamDependency) {
        this.upstreamDependency = upstreamDependency;
    }

    public String getUpstreamDependency() {
        return upstreamDependency;
    }
}
