package nc.message;

public class RegisterStatus extends NCMessage {
    public long clientID;

    public RegisterStatus() {
    }

    public RegisterStatus(long clientID) {
        this.clientID = clientID;
    }

    @Override
    public PacketType type() {
        return PacketType.REGISTER_STATUS;
    }

}
