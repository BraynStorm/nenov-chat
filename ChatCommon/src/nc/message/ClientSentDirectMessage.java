package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientSentDirectMessage extends NCMessage {
    /**
     * Sender or Receiver
     */
    public long id;
    public byte[] message;

    public ClientSentDirectMessage() {
    }

    public ClientSentDirectMessage(long id, String message) throws PacketCorruptionException {
        this.id = id;

        this.message = message.getBytes(NCMessage.Charset());
        if (this.message.length > messageMaxSize())
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
        super.validatePostRead();
        NetUtil.Read.Check(message.length <= messageMaxSize());
    }
}
