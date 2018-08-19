package launchserver.auth;

import launchserver.LaunchServer;

public class HWIDHandler {
	public final HWID nullHWID = HWID.gen(0, 0, 0);
	public HWIDHandler(LaunchServer srv) {
		
	}
	
	public void handle(HWID hwid, String username) throws AuthException {
		if (nullHWID.equals(hwid)) return;
		
	}
}
