package nc.message;

import nc.exc.PacketCorruptionException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientAuthenticate implements NCMessage {

    private long sessionID;
    private byte[] email;
    private byte[] password;

    public ClientAuthenticate(long sessionID, String email, String password) throws PacketCorruptionException {
        this.sessionID = sessionID;
        
        this.email = email.getBytes(charset);
        if (this.email.length > maximumEmailSize())
            throw new PacketCorruptionException();

        this.password = password.getBytes(charset);
        if (this.password.length > maximumPasswordSize())
            throw new PacketCorruptionException();
    }

    @Override
    public boolean toBytes(ByteBuffer destination) {


    }

    @Override
    public void fromBytes(ByteBuffer source) throws IOException {

    }

    public int fixedSize() {
        return 2 + 4 + 4 + 4;
    }

    public int maximumEmailSize() {
        return 128;
    }

    public int maximumPasswordSize() {
        return 128;
    }

    @Override
    public int maximumSize() {
        return fixedSize() + maximumEmailSize() + maximumPasswordSize();
    }
}
