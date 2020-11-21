package nc.message;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ConnectSuccessful implements NCMessage {
    public long sessionID;

    @Override
    public boolean toBytes(ByteBuffer destination) {
        if (destination.remaining() < maximumSize())
            return false;
        else {
            destination.putShort((short) PacketType.CONNECT_SUCCESSFUL.ordinal());
            destination.putInt(maximumSize());
            destination.putLong(sessionID);
        }
    }

    @Override
    public void fromBytes(ByteBuffer source) throws IOException {

    }

    @Override
    public int maximumSize() {
        return 2 + 4 + 8;
    }
}
