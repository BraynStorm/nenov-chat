package nc.message;

import nc.exc.PacketCorruptionException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientAuthenticate implements NCMessage {
    public long sessionID;
    public byte[] email;
    public byte[] password;

    public ClientAuthenticate() {
    }

    public ClientAuthenticate(long sessionID, String email, String password) throws PacketCorruptionException {
        this.sessionID = sessionID;

        this.email = email.getBytes(charset);
        if (this.email.length > maximumEmailSize())
            throw new PacketCorruptionException();

        this.password = password.getBytes(charset);
        if (this.password.length > maximumPasswordSize())
            throw new PacketCorruptionException();
    }

    public String getEmail() {
        return new String(email, charset);
    }

    public String getPassword() {
        return new String(password, charset);
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
        return PacketType.CLIENT_AUTHENTICATE;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(email.length <= maximumEmailSize());
        NetUtil.Read.Check(password.length <= maximumPasswordSize());
    }
}

