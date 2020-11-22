package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientSentDirectMessage implements NCMessage {

    public long clientID;
    public byte[] message;

    public ClientSentDirectMessage() {
    }

    public ClientSentDirectMessage(long clientID, String message) throws PacketCorruptionException {
        this.clientID = clientID;

        this.message = message.getBytes(NCMessage.Charset());
        if (this.message.length > maximumSize())
            throw new PacketCorruptionException();
    }

    public int messageMaxSize() {
        return maximumSize() - fixedSize();
    }

    @Override
    public int maximumSize() {
        return 1024;
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_SEND_DIRECT_MESSAGE;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(message.length <= messageMaxSize());
    }
}
