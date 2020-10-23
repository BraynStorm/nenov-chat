package nc.message;

import nc.exc.PacketCorruptionException;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class Ping implements Serializable, NCMessage {
    public long time = 0;

    @Override
    public boolean toBytes(ByteBuffer destination) {
        if (destination.remaining() >= maximumSize()) {
            destination.putShort((short) PacketList.PING.ordinal());
            destination.putInt(maximumSize());
            destination.putLong(time);
            return true;
        } else
            return false;
    }

    @Override
    public void fromBytes(ByteBuffer source) throws IOException {
        short id = source.getShort();

        if (id != (short) PacketList.PING.ordinal())
            throw new PacketCorruptionException();

        time = source.getLong();
    }

    @Override
    public int maximumSize() {
        return 2 + 4 + 8;
    }
}
