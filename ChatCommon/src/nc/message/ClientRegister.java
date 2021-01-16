package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientRegister implements NCMessage {
    public long sessionID;

    public byte[] email;
    public byte[] password;

    public ClientRegister() {
    }

    public ClientRegister(long sessionID, byte[] email, byte[] password) {
        this.sessionID = sessionID;
        this.email = email;
        this.password = password;
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
        return PacketType.CLIENT_REGISTER;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(email.length <= maximumEmailSize());
        NetUtil.Read.Check(password.length <= maximumPasswordSize());
    }
}
