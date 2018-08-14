package launchserver.response.netty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import launcher.Launcher;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.PingResponse;
import launchserver.response.Response;
import launchserver.response.ResponseThread;
import launchserver.response.auth.AuthResponse;
import launchserver.response.auth.CheckServerResponse;
import launchserver.response.auth.JoinServerResponse;
import launchserver.response.profile.BatchProfileByUsernameResponse;
import launchserver.response.profile.ProfileByUUIDResponse;
import launchserver.response.profile.ProfileByUsernameResponse;
import launchserver.response.update.LauncherResponse;
import launchserver.response.update.UpdateListResponse;
import launchserver.response.update.UpdateResponse;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

public class LauncherNettyHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
    LaunchServer server;
    public LauncherNettyHandler(LaunchServer s)
    {
        server = s;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        System.out.println("Th");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {
        // Generate and write a response.
        boolean close = false;
        ByteBuf buf = (ByteBuf) request;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        System.out.println(Arrays.toString(bytes));
        HInput hInput = new HInput(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HOutput hOutput = new HOutput(os);
        Request.Type t = readHandshake(hInput,hOutput);
        System.out.println(t.getNumber());
        respond(t,hInput,hOutput,0);
        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(os.toByteArray());

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("ReadComplete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private Request.Type readHandshake(HInput input, HOutput output) throws IOException {
        boolean legacy = false;

        // Verify magic number
        int magicNumber = input.readInt();
        if (magicNumber != Launcher.PROTOCOL_MAGIC) {
            if (magicNumber != Launcher.PROTOCOL_MAGIC - 1) { // Previous launcher protocol
                output.writeBoolean(false);
                throw new IOException(String.format("Protocol magic mismatch"));
            }
            legacy = true;
        }

        // Verify key modulus
        BigInteger keyModulus = input.readBigInteger(SecurityHelper.RSA_KEY_LENGTH + 1);
        if (!keyModulus.equals(server.privateKey.getModulus())) {
            output.writeBoolean(false);
            throw new IOException(String.format("Key modulus mismatch"));
        }

        // Read request type
        Request.Type type = Request.Type.read(input);
        if (legacy && type != Request.Type.LAUNCHER) {
            output.writeBoolean(false);
            throw new IOException(String.format("Not LAUNCHER request on legacy protocol"));
        }
        //if (!server.serverSocketHandler.onHandshake(id, type)) {
        //    output.writeBoolean(false);
        //    return null;
        //}

        // Protocol successfully verified
        output.writeBoolean(true);
        output.flush();
        return type;
    }

    private void respond(Request.Type type, HInput input, HOutput output, long id) throws Exception {
        // Choose response based on type
        Response response;
        switch (type) {
            case PING:
                response = new PingResponse(server, id, input, output);
                break;
            case AUTH:
                response = new AuthResponse(server, id, input, output, "");
                break;
            case JOIN_SERVER:
                response = new JoinServerResponse(server, id, input, output);
                break;
            case CHECK_SERVER:
                response = new CheckServerResponse(server, id, input, output);
                break;
            case LAUNCHER:
                response = new LauncherResponse(server, id, input, output);
                break;
            case UPDATE:
                response = new UpdateResponse(server, id, input, output);
                break;
            case UPDATE_LIST:
                response = new UpdateListResponse(server, id, input, output);
                break;
            case PROFILE_BY_USERNAME:
                response = new ProfileByUsernameResponse(server, id, input, output);
                break;
            case PROFILE_BY_UUID:
                response = new ProfileByUUIDResponse(server, id, input, output);
                break;
            case BATCH_PROFILE_BY_USERNAME:
                response = new BatchProfileByUsernameResponse(server, id, input, output);
                break;
            case CUSTOM:
                String name = VerifyHelper.verifyIDName(input.readASCII(255));
                response = server.serverSocketHandler.newCustomResponse(name, id, input, output);
                break;
            default:
                throw new AssertionError("Unsupported request type: " + type.name());
        }

        // Reply
        response.reply();
        LogHelper.subDebug("#%d Replied", id);
    }
}
