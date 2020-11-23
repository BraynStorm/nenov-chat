package nc.message;

public class ClientAuthenticationStatus implements NCMessage {
    public long clientID = 0;
    
    @Override
    public PacketType type() {
        return PacketType.CLIENT_AUTHENTICATION_STATUS;
    }
}
