package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientSentDirectMessage implements NCMessage {

    public long clientID;
    public byte[] message;

    public int maximumSize() {
        return 1024;
    }

    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
    }
}
