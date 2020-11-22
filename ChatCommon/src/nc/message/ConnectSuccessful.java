package nc.message;

import nc.exc.PacketCorruptionException;

public class ConnectSuccessful implements NCMessage {
    public long sessionID;

    public ConnectSuccessful(long sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public int maximumSize() {
        return fixedSize();
    }

    @Override
    public PacketType type() {
        return PacketType.CONNECT_SUCCESSFUL;
    }
}