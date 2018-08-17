package launchserver.response;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;

import launcher.Launcher;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request.Type;
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

public final class ResponseThread implements Runnable {
    private final LaunchServer server;
    private final Socket socket;

    public ResponseThread(LaunchServer server, long id, Socket socket) throws SocketException {
        this.server = server;
        this.socket = socket;

        // Fix socket flags
        IOHelper.setSocketFlags(socket);
    }

    @Override
    public void run() {
        if (!server.serverSocketHandler.logConnections) {
            LogHelper.debug("Connection from %s", IOHelper.getIP(socket.getRemoteSocketAddress()));
        }

        // Process connection
        boolean cancelled = false;
        Exception savedError = null;
        try (HInput input = new HInput(socket.getInputStream());
            HOutput output = new HOutput(socket.getOutputStream())) {
            Handshake handshake = readHandshake(input, output);
            if (handshake == null) { // Not accepted
                cancelled = true;
                return;
            }

            // Start response
            try {
                respond(handshake.type, input, output,handshake.session);
            } catch (RequestException e) {
                LogHelper.subDebug(String.format("#%d Request error: %s", handshake.session, e.getMessage()));
                output.writeString(e.getMessage(), 0);
            }
        } catch (Exception e) {
            savedError = e;
            LogHelper.error(e);
        } finally {
            IOHelper.close(socket);
            if (!cancelled) {
                server.serverSocketHandler.onDisconnect(savedError);
            }
        }
    }
    class Handshake
    {
        Type type;
        long session;

        public Handshake(Type type, long session) {
            this.type = type;
            this.session = session;
        }
    }
    private Handshake readHandshake(HInput input, HOutput output) throws IOException {
        boolean legacy = false;
        long session = 0;
        // Verify magic number
        int magicNumber = input.readInt();
        if (magicNumber != Launcher.PROTOCOL_MAGIC) {
            if (magicNumber != Launcher.PROTOCOL_MAGIC - 1) { // Previous launcher protocol
                session = 0;
            }
            session = 0;
            legacy = true;
        }
        // Verify key modulus
        BigInteger keyModulus = input.readBigInteger(SecurityHelper.RSA_KEY_LENGTH + 1);
        if(!legacy)
        {
            session = input.readLong();
        }
        if (!keyModulus.equals(server.privateKey.getModulus())) {
            output.writeBoolean(false);
            throw new IOException(String.format("#%d Key modulus mismatch", session));
        }
        // Read request type
        Type type = Type.read(input);
        if (!server.serverSocketHandler.onHandshake(session,type)) {
            output.writeBoolean(false);
            return null;
        }

        // Protocol successfully verified
        output.writeBoolean(true);
        output.flush();
        return new Handshake(type,session);
    }

    private void respond(Type type, HInput input, HOutput output,long session) throws Exception {
        if (server.serverSocketHandler.logConnections) {
            LogHelper.info("Connection #%d from %s: %s", session, IOHelper.getIP(socket.getRemoteSocketAddress()), type.name());
        } else {
            LogHelper.subDebug("#%d Type: %s", session, type.name());
        }

        // Choose response based on type
        Response response;
        switch (type) {
            case PING:
                response = new PingResponse(server, session, input, output);
                break;
            case AUTH:
                response = new AuthResponse(server, session, input, output, IOHelper.getIP(socket.getRemoteSocketAddress()));
                break;
            case JOIN_SERVER:
                response = new JoinServerResponse(server, session, input, output);
                break;
            case CHECK_SERVER:
                response = new CheckServerResponse(server, session, input, output);
                break;
            case LAUNCHER:
                response = new LauncherResponse(server, session, input, output);
                break;
            case UPDATE:
                response = new UpdateResponse(server, session, input, output);
                break;
            case UPDATE_LIST:
                response = new UpdateListResponse(server, session, input, output);
                break;
            case PROFILE_BY_USERNAME:
                response = new ProfileByUsernameResponse(server, session, input, output);
                break;
            case PROFILE_BY_UUID:
                response = new ProfileByUUIDResponse(server, session, input, output);
                break;
            case BATCH_PROFILE_BY_USERNAME:
                response = new BatchProfileByUsernameResponse(server, session, input, output);
                break;
            case CUSTOM:
                String name = VerifyHelper.verifyIDName(input.readASCII(255));
                response = server.serverSocketHandler.newCustomResponse(name, session, input, output);
                break;
            default:
                throw new AssertionError("Unsupported request type: " + type.name());
        }

        // Reply
        response.reply();
        LogHelper.subDebug("#%d Replied", session);
    }
}
