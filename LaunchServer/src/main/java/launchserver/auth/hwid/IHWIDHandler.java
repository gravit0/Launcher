package launchserver.auth.hwid;

import launchserver.LaunchServer;
import launchserver.auth.AuthException;

public interface IHWIDHandler {
	public void start(LaunchServer srv);
	public void handle(HWID hwid, String username) throws AuthException;
	public void stop();
}
