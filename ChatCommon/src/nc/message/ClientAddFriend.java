package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientAddFriend extends NCMessage {
    public byte[] email;

    public ClientAddFriend() {
    }

    public ClientAddFriend(String email) throws PacketCorruptionException {
        this.email = email.getBytes(Charset());

        if (this.email.length > maximumEmailSize())
            throw new PacketCorruptionException();
    }

    public String getEmail() {
        return new String(email, Charset());
    }

    public int maximumEmailSize() {
        return 128;
    }

    @Override
    public int maximumSize() {
        return fixedSize() + maximumEmailSize();
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_ADD_FRIEND;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        super.validatePostRead();

        NetUtil.Read.Check(email.length <= maximumEmailSize());
    }
}
