package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientResponseClientName extends NCMessage {
    public byte[] name;

    public ClientResponseClientName() {
    }

    public ClientResponseClientName(String name) throws PacketCorruptionException {
        this.name = name.getBytes(Charset());

        if (this.name.length > maximumNameLength())
            throw new PacketCorruptionException();
    }

    public String getName() {
        return new String(name, Charset());
    }

    public int maximumNameLength() {
        return 256;
    }

    @Override
    public int maximumSize() {
        return fixedSize() + maximumNameLength();
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_RESPONSE_CLIENT_NAME;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        super.validatePostRead();
        NetUtil.Read.Check(name.length <= maximumNameLength());
    }
}
