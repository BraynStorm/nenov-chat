package nc.message;

import nc.exc.PacketCorruptionException;

public class Authenticate implements NCMessage {
    public long sessionID;
    public byte[] email;
    public byte[] password;

    public Authenticate() {
    }

    public Authenticate(long sessionID, String email, String password) throws PacketCorruptionException {
        this.sessionID = sessionID;

        this.email = email.getBytes(NCMessage.Charset());
        if (this.email.length > maximumEmailSize())
            throw new PacketCorruptionException();

        this.password = password.getBytes(NCMessage.Charset());
        if (this.password.length > maximumPasswordSize())
            throw new PacketCorruptionException();
    }

    public String getEmail() {
        return new String(email, NCMessage.Charset());
    }

    public String getPassword() {
        return new String(password, NCMessage.Charset());
    }

    public static int maximumEmailSize() {
        return 128;
    }

    public static int maximumPasswordSize() {
        return 128;
    }

    @Override
    public int maximumSize() {
        return fixedSize() + maximumEmailSize() + maximumPasswordSize();
    }

    @Override
    public PacketType type() {
        return PacketType.AUTHENTICATE;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(email.length <= maximumEmailSize());
        NetUtil.Read.Check(password.length <= maximumPasswordSize());
    }
}

