package nc.message;

import nc.exc.PacketCorruptionException;

public class Register implements NCMessage {
    public long sessionID;

    public byte[] email;
    public byte[] password;

    public Register() {
    }

    public Register(long sessionID, String email, String password) throws PacketCorruptionException {
        this.sessionID = sessionID;
        this.email = email.getBytes(NCMessage.Charset());
        this.password = password.getBytes(NCMessage.Charset());

        if (email.length() > maximumEmailSize())
            throw new PacketCorruptionException();
        if (password.length() > maximumPasswordSize())
            throw new PacketCorruptionException();
    }

    public long getSessionID() {
        return sessionID;
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
        return PacketType.REGISTER;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(email.length <= maximumEmailSize());
        NetUtil.Read.Check(password.length <= maximumPasswordSize());
    }
}
