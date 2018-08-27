package launchserver.socket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;

import launcher.Launcher;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.request.RequestException;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.manangers.SessionManager;
import launchserver.response.Response;

public final class ResponseThread implements Runnable {
    private final LaunchServer server;
    private final Socket socket;
    private final SessionManager sessions;

    public ResponseThread(LaunchServer server, long id, Socket socket, SessionManager sessionManager) throws SocketException {
        this.server = server;
        this.socket = socket;
        this.sessions = sessionManager;
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
                respond(handshake.type, input, output, handshake.session, IOHelper.getIP(socket.getRemoteSocketAddress()));
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

    class Handshake {
        int type;
        long session;

        public Handshake(int type, long session) {
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
            if (magicNumber != Launcher.PROTOCOL_MAGIC_LEGACY - 1) { // Previous launcher protocol
                session = 0;
                legacy = true;
            }
            else if (magicNumber != Launcher.PROTOCOL_MAGIC_LEGACY){
                sessions.updateClient(session);
            }

        }
        else {
            sessions.updateClient(session);
        }
        // Verify key modulus
        BigInteger keyModulus = input.readBigInteger(SecurityHelper.RSA_KEY_LENGTH + 1);
        if (!legacy) {
            session = input.readLong();
        }
        if (!keyModulus.equals(server.privateKey.getModulus())) {
            output.writeBoolean(false);
            throw new IOException(String.format("#%d Key modulus mismatch", session));
        }
        // Read request type
        Integer type = input.readVarInt();
        if (!server.serverSocketHandler.onHandshake(session, type)) {
            output.writeBoolean(false);
            return null;
        }

        // Protocol successfully verified
        output.writeBoolean(true);
        output.flush();
        return new Handshake(type, session);
    }

    private void respond(Integer type, HInput input, HOutput output, long session, String ip) throws Exception {
        if (server.serverSocketHandler.logConnections) {
            LogHelper.info("Connection #%d from %s", session, ip);
        }

        // Choose response based on type
        Response response = Response.getResponse(type, server, session, input, output, ip);

        // Reply
        response.reply();
        LogHelper.subDebug("#%d Replied", session);
    }
}
