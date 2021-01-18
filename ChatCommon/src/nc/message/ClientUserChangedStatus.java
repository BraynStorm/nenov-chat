package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientUserChangedStatus extends NCMessage {
    public long userID;
    public byte[] email;
    public boolean online;

    public ClientUserChangedStatus() {
    }

    public ClientUserChangedStatus(long userID, String email, boolean online) throws PacketCorruptionException {
        this.userID = userID;
        this.email = email.getBytes(Charset());
        this.online = online;

        if (this.email.length > maximumEmailLength())
            throw new PacketCorruptionException();
    }

    public String getEmail() {
        return new String(email, Charset());
    }

    public int maximumEmailLength() {
        return 128;
    }

    @Override
    public int maximumSize() {
        return fixedSize() + maximumEmailLength();
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_USER_CHANGED_STATUS;
    }
}
