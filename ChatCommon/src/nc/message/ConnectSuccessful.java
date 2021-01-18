package nc.message;

public class ConnectSuccessful extends NCMessage {
    public long sessionID;

    public ConnectSuccessful() {
    }

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
