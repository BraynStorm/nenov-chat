package nc.message;

public class ClientRequestClientName extends NCMessage{
    public long clientID;

    public ClientRequestClientName() {
    }

    public ClientRequestClientName(long clientID) {
        this.clientID = clientID;
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_REQUEST_CLIENT_NAME;
    }
}
