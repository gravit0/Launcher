package launchserver.response.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface RequestWorker {
    void request(ByteBuf buf, ChannelHandlerContext ctx);
}
