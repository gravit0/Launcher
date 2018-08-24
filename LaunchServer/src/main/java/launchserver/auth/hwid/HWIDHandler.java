package launchserver.auth.hwid;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;

public abstract class HWIDHandler extends ConfigObject implements AutoCloseable {
    private static final Map<String, Adapter<HWIDHandler>> HW_HANDLERS = new ConcurrentHashMap<>(4);
    public static final HWID nullHWID = HWID.gen(0, 0, 0);
    private static boolean registredHandl = false;

    protected HWIDHandler(BlockConfigEntry block) {
        super(block);
    }

    public abstract void check0(HWID hwid, String username) throws HWIDException;
    public abstract void ban(List<HWID> hwid) throws HWIDException;
    public abstract void unban(List<HWID> hwid) throws HWIDException;
    public abstract List<HWID> getHwid(String username) throws HWIDException;

    public void check(HWID hwid, String username) throws HWIDException {
        if (nullHWID.equals(hwid)) return;
        check0(hwid, username);
    }

    @Override
    public abstract void close() throws IOException;

    @LauncherAPI
    public static HWIDHandler newHandler(String name, BlockConfigEntry block) {
        Adapter<HWIDHandler> authHandlerAdapter = VerifyHelper.getMapValue(HW_HANDLERS, name,
                String.format("Unknown HWID handler: '%s'", name));
        return authHandlerAdapter.convert(block);
    }

    @LauncherAPI
    public static void registerHandler(String name, Adapter<HWIDHandler> adapter) {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(HW_HANDLERS, name, Objects.requireNonNull(adapter, "adapter"),
                String.format("HWID handler has been already registered: '%s'", name));
    }

    public static void registerHandlers() {
        if (!registredHandl) {
            registerHandler("accept", AcceptHWIDHandler::new);
            registredHandl = true;
        }
    }
}
