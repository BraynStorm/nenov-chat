package nc.message;

import nc.exc.PacketCorruptionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ClientJoinRoom implements NCMessage {
    public long clientID;
    private byte[] nameBytes;

    public ClientJoinRoom(long clientID, String name) throws PacketCorruptionException {
        byte[] nameBytes = name.getBytes(charset);

        if (nameBytes.length > stringMaxSize())
            throw new PacketCorruptionException();

        this.clientID = clientID;
        this.nameBytes = nameBytes;
    }

    public String decodeName() {
        return charset.decode(ByteBuffer.wrap(nameBytes)).toString();
    }

    @Override
    public boolean toBytes(ByteBuffer destination) {
        if (destination.remaining() < fixedSize() + nameBytes.length)
            return false;
        else {
            destination.putShort((short) PacketList.CLIENT_JOIN_ROOM.ordinal());
            destination.putInt(fixedSize() + nameBytes.length);
            destination.putLong(clientID);
            destination.put(nameBytes);
            return true;
        }
    }

    @Override
    public void fromBytes(ByteBuffer source) throws IOException {
        short id = source.getShort();
        if (id != (short) PacketList.CLIENT_JOIN_ROOM.ordinal())
            throw new PacketCorruptionException();

        int packetSize = source.getInt();
        if (packetSize > maximumSize())
            throw new PacketCorruptionException();

        clientID = source.getLong();
        nameBytes = new byte[packetSize - fixedSize()];
        source.get(nameBytes);
    }

    public int fixedSize() {
        return 2 + 4 + 8;
    }

    public int stringMaxSize() {
        return maximumSize() - fixedSize();
    }

    @Override
    public int maximumSize() {
        return 256;
    }
}
