package launchserver.response;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.helper.LogHelper;
import launcher.request.Request;
import launcher.request.RequestException;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.auth.AuthResponse;
import launchserver.response.auth.CheckServerResponse;
import launchserver.response.auth.JoinServerResponse;
import launchserver.response.profile.BatchProfileByUsernameResponse;
import launchserver.response.profile.ProfileByUUIDResponse;
import launchserver.response.profile.ProfileByUsernameResponse;
import launchserver.response.update.LauncherResponse;
import launchserver.response.update.UpdateListResponse;
import launchserver.response.update.UpdateResponse;

public abstract class Response {
    @LauncherAPI protected final LaunchServer server;
    @LauncherAPI protected final HInput input;
    @LauncherAPI protected final HOutput output;
    @LauncherAPI protected final String ip;
    @LauncherAPI protected final long session;
    private static final Map<Integer, Factory> RESPONSES = new ConcurrentHashMap<>(8);
    protected Response(LaunchServer server,long session, HInput input, HOutput output,String ip) {
        this.server = server;
        this.input = input;
        this.output = output;
        this.ip = ip;
        this.session = session;
    }
    public static void registerResponse(int type, Factory factory)
    {
        RESPONSES.put(type,factory);
    }
    public static void registerResponses()
    {
        registerResponse(Request.Type.PING.getNumber(),PingResponse::new);
        registerResponse(Request.Type.AUTH.getNumber(),AuthResponse::new);
        registerResponse(Request.Type.CHECK_SERVER.getNumber(), CheckServerResponse::new);
        registerResponse(Request.Type.JOIN_SERVER.getNumber(), JoinServerResponse::new);

        registerResponse(Request.Type.BATCH_PROFILE_BY_USERNAME.getNumber(), BatchProfileByUsernameResponse::new);
        registerResponse(Request.Type.PROFILE_BY_USERNAME.getNumber(), ProfileByUsernameResponse::new);
        registerResponse(Request.Type.PROFILE_BY_UUID.getNumber(), ProfileByUUIDResponse::new);

        registerResponse(Request.Type.LAUNCHER.getNumber(), LauncherResponse::new);
        registerResponse(Request.Type.UPDATE_LIST.getNumber(), UpdateListResponse::new);
        registerResponse(Request.Type.UPDATE.getNumber(), UpdateResponse::new);
    }
    public static Response getResponse(int type, LaunchServer server,long session,HInput input,HOutput output,String ip)
    {
        return RESPONSES.get(type).newResponse(server,session,input,output,ip);
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
    public interface Factory<R> {
        @LauncherAPI
        Response newResponse(LaunchServer server, long id, HInput input, HOutput output,String ip);
    }
}
