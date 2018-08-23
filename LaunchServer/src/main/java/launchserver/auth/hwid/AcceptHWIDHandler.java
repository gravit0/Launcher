package launchserver.auth.hwid;

import launcher.serialize.config.entry.BlockConfigEntry;

public class AcceptHWIDHandler extends HWIDHandler {

    public AcceptHWIDHandler(BlockConfigEntry block) {
        super(block);
    }

    @Override
    public void check0(HWID hwid, String username) {
        //SKIP
    }

    @Override
    public void ban(HWID hwid) {
        //SKIP
    }

    @Override
    public void unban(HWID hwid) {
        //SKIP
    }

    @Override
    public HWID getHwid(String username) {
        return nullHWID;
    }

    @Override
    public void close() {
        //SKIP
    }

}
