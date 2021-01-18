package nc.message;

public class AuthenticationStatus implements NCMessage {
    public long clientID = -1;


    public AuthenticationStatus() {
    }

    public AuthenticationStatus(long clientID) {
        this.clientID = clientID;
    }

    @Override
    public PacketType type() {
        return PacketType.AUTHENTICATION_STATUS;
    }
}
