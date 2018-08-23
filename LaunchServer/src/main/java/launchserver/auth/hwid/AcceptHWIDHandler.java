package launchserver.auth.hwid;

import java.io.IOException;

import launcher.serialize.config.entry.BlockConfigEntry;
import launchserver.auth.AuthException;

public class AcceptHWIDHandler extends HWIDHandler {

    public AcceptHWIDHandler(BlockConfigEntry block) {
        super(block);
    }

    @Override
    public void check0(HWID hwid, String username) throws AuthException {

    }

    @Override
    public void close() throws IOException {

    }

}
