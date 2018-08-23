package launchserver.auth.hwid;

import java.util.ArrayList;
import java.util.List;

import launchserver.LaunchServer;
import launchserver.auth.AuthException;

public class HWIDCoreHandler implements IHWIDHandler {
	public final HWID nullHWID = HWID.gen(0, 0, 0);
	public final List<IHWIDHandler> handlers;
	private final LaunchServer srv;
	public HWIDCoreHandler(LaunchServer srv) {
		this.srv = srv;
		start(srv);
		handlers = new ArrayList<IHWIDHandler>();
	}
	
	public void add(IHWIDHandler handler) {
		handler.start(srv);
		handlers.add(handler);
	}
	
	@Override
	public void handle(HWID hwid, String username) throws AuthException {
		if (nullHWID.equals(hwid)) return;
		for (IHWIDHandler handler : handlers) handler.handle(hwid, username);
	}

	@Override
	public void start(LaunchServer srv) { }

	@Override
	public void stop() {
		for (IHWIDHandler handler : handlers) handler.stop();
	}
}
