package nc.message;

import nc.exc.PacketCorruptionException;

public class ConnectSuccessful implements NCMessage {
    public long sessionID;

    @Override
    public int maximumSize() {
        return fixedSize();
    }

    @Override
    public PacketType type() {
        return PacketType.CONNECT_SUCCESSFUL;
    }
}
