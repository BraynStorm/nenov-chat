package nc.message;

public class ClientAuthenticationStatus implements NCMessage {
    public long clientID = -1;


    public ClientAuthenticationStatus() {
    }

    public ClientAuthenticationStatus(long clientID) {
        this.clientID = clientID;
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_AUTHENTICATION_STATUS;
    }
}
