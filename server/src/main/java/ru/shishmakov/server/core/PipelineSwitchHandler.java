package ru.shishmakov.server.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.shishmakov.server.helper.ProtocolType;

import java.lang.invoke.MethodHandles;
import java.util.List;

public abstract class PipelineSwitchHandler extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles
            .lookup().lookupClass());

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf message, final List<Object> ignore) throws Exception {
        // Will use the first four bytes to detect a protocol.
        if (message.readableBytes() < 4) {
            return;
        }
        switch (getProtocolType(message)) {
            case HTTP:
                logger.debug("HTTP protocol pipeline is enabled");
                enableHttpPipeline(ctx);
                break;
            case PROTOCOL_BUFFER:
                logger.debug("Protocol Buffer pipeline is enabled");
                enableProtocolBufferPipeline(ctx);
                break;
            default:
                logger.debug("Unknown protocol: discard everything and close the connection");
                message.clear();
                ctx.close();
                break;
        }
    }

    protected abstract void enableProtocolBufferPipeline(final ChannelHandlerContext ctx);

    public abstract void enableHttpPipeline(final ChannelHandlerContext ctx);

    private ProtocolType getProtocolType(final ByteBuf message) {
        final short firstByte = message.readUnsignedByte();
        final short secondByte = message.readUnsignedByte();
        final short thirdByte = message.readUnsignedByte();
        final short fourthByte = message.readUnsignedByte();

        if (firstByte == 'H' && secondByte == 'T' && thirdByte == 'T' & fourthByte == 'P') {
            return ProtocolType.HTTP;
        }
        if (firstByte == 'P' && secondByte == 'R' && thirdByte == 'B' & fourthByte == 'F') {
            return ProtocolType.PROTOCOL_BUFFER;
        }
        return ProtocolType.UNKNOWN;
    }


}