package launchserver;

public interface Module {
    public String getName();
    public String getVersion();
    public void init();
	public void preInit();
}
