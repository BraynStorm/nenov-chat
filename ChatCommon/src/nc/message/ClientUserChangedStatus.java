package nc.message;

public class ClientUserChangedStatus extends NCMessage {
    public long userID;
    public boolean online;

    @Override
    public PacketType type() {
        return PacketType.CLIENT_USER_CHANGED_STATUS;
    }
}
