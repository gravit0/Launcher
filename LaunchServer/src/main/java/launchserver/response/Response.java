package launchserver.response;

import java.io.IOException;
import java.net.Socket;

import launcher.LauncherAPI;
import launcher.helper.LogHelper;
import launcher.request.RequestException;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;

public abstract class Response {
    @LauncherAPI protected final LaunchServer server;
    @LauncherAPI protected final HInput input;
    @LauncherAPI protected final HOutput output;
    @LauncherAPI protected final Socket socket;
    @LauncherAPI protected final long session;

    protected Response(LaunchServer server,long session, HInput input, HOutput output,Socket socket) {
        this.server = server;
        this.input = input;
        this.output = output;
        this.socket = socket;
        this.session = session;
    }

    @LauncherAPI
    protected Response(LaunchServer server,long session, HInput input, HOutput output) {
        this.server = server;
        this.input = input;
        this.output = output;
        this.socket = null;
        this.session = session;
    }

    @LauncherAPI
    public abstract void reply() throws Exception;

    @LauncherAPI
    protected final void debug(String message) {
        LogHelper.subDebug("#%d %s", session, message);
    }

    @LauncherAPI
    protected final void debug(String message, Object... args) {
        debug(String.format(message, args));
    }

    @LauncherAPI
    @SuppressWarnings("MethodMayBeStatic") // Intentionally not static
    protected final void writeNoError(HOutput output) throws IOException {
        output.writeString("", 0);
    }

    @LauncherAPI
    public static void requestError(String message) throws RequestException {
        throw new RequestException(message);
    }

    @FunctionalInterface
    public interface Factory {
        @LauncherAPI
        Response newResponse(LaunchServer server, long id, HInput input, HOutput output);
    }
}
