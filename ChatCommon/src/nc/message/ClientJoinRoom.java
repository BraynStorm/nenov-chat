package nc.message;

import nc.exc.PacketCorruptionException;

public class ClientJoinRoom implements NCMessage {
    public long clientID;
    public byte[] name;

    public ClientJoinRoom() {
    }

    public ClientJoinRoom(long clientID, String name) throws PacketCorruptionException {
        this.clientID = clientID;

        this.name = name.getBytes(NCMessage.Charset());
        if (this.name.length > stringMaxSize())
            throw new PacketCorruptionException();

    }

    public String getName() {
        return new String(name, NCMessage.Charset());
    }

    public int stringMaxSize() {
        return maximumSize() - fixedSize();
    }

    @Override
    public int maximumSize() {
        return 256;
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_JOIN_ROOM;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        NetUtil.Read.Check(name.length <= stringMaxSize());
    }
}
