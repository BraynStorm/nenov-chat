package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientRegisterStatus implements NCMessage {
    public long clientID;

    public ClientRegisterStatus() {
    }

    public ClientRegisterStatus(long clientID) {
        this.clientID = clientID;
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_REGISTER_STATUS;
    }

}
