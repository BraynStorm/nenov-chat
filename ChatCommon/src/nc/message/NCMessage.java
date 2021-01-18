package nc.message;

import nc.exc.PacketCorruptionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class NCMessage {
    public static Charset Charset() {
        return Charset.forName("UTF-8");
    }

    public boolean toBytes(ByteBuffer destination) {
        return NetUtil.Write.AutoWrite(destination, this);
    }

    public void fromBytes(ByteBuffer source) throws IOException {
        NetUtil.Read.AutoRead(source, this);

    }

    public int maximumSize() {
        return fixedSize();
    }

    public int fixedSize() {
        return NetUtil.SizeOf(this.getClass());
    }

    public int size() {
        return NetUtil.TotalSizeOf(this);
    }

    public abstract PacketType type();

    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(size() <= maximumSize());
    }
}
