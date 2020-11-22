package nc.message;

import nc.exc.PacketCorruptionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface NCMessage {
    static Charset Charset() {
        return Charset.forName("UTF-8");
    }

    default boolean toBytes(ByteBuffer destination) {
        return NetUtil.Write.AutoWrite(destination, this);
    }

    default void fromBytes(ByteBuffer source) throws IOException {
        NetUtil.Read.AutoRead(source, this);

    }

    int maximumSize();

    default int fixedSize() {
        return NetUtil.SizeOf(this.getClass());
    }

    default int size() {
        return NetUtil.TotalSizeOf(this);
    }

    PacketType type();

    default void validatePostRead() throws PacketCorruptionException {
    }
}
