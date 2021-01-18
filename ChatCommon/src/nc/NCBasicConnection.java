package nc;

import nc.exc.ConnectionClosed;
import nc.exc.PacketCorruptionException;
import nc.message.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NCBasicConnection {
    private SocketChannel channel;
    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;
    private final BlockingQueue<NCMessage> writeQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<NCMessage> readQueue = new LinkedBlockingQueue<>();
    private long lastRead;
    private long lastWrite;

    protected long sessionID = -1;
    protected long clientID = -1;

    private void updateReadTime() {
        lastRead = System.nanoTime();
    }

    private void updateWriteTime() {
        lastRead = System.nanoTime();
    }

    void read() throws IOException {
        if (readBuffer.hasRemaining()) {
            int read = channel.read(readBuffer);
            if (read != 0) {
                updateReadTime();
            }

            if (read == -1)
                close();
        }
    }

    void write() throws IOException {
        if (writeBuffer.position() > 0) {
            writeBuffer.flip();
            int written = channel.write(writeBuffer);
            writeBuffer.compact();
            if (written > 0)
                updateWriteTime();
        }
    }

    void processReadPackets() {
        readBuffer.flip();
        while (readBuffer.remaining() >= 6) {
            short packetID = readBuffer.getShort(0);
            int packetSize = readBuffer.getInt(2);

            if (readBuffer.remaining() >= packetSize) {
                try {
                    processReadPacket(packetID);
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    break;
                }
            } else
                break;
        }
        readBuffer.compact();
    }

    void processWritePackets() {
        if (writeBuffer.position() == writeBuffer.capacity()) return;

        while (writeQueue.peek() != null) {
            NCMessage message = writeQueue.peek();
            assert message != null;

            if (message.maximumSize() > writeBuffer.capacity())
                growWriteBuffer(message.maximumSize());

            if (message.size() <= writeBuffer.remaining())
                if (message.toBytes(writeBuffer))
                    writeQueue.remove();
                else
                    break;
        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
        writeBuffer.clear();
        readBuffer.clear();
    }

    private void processReadPacket(short packetID) throws IOException {
        NCMessage packet;

        switch (PacketType.values()[packetID]) {
            case PING:
                packet = new Ping();
                break;
            case AUTHENTICATE:
                packet = new Authenticate();
                break;
            case CONNECT_SUCCESSFUL:
                packet = new ConnectSuccessful();
                break;
            case CLIENT_SEND_DIRECT_MESSAGE:
                packet = new ClientSentDirectMessage();
                break;
            case CLIENT_JOIN_ROOM:
                packet = new ClientJoinRoom();
                break;
            case AUTHENTICATION_STATUS:
                packet = new AuthenticationStatus();
                break;
            case REGISTER:
                packet = new Register();
                break;
            case REGISTER_STATUS:
                packet = new RegisterStatus();
                break;
            default:
                throw new PacketCorruptionException();
        }

        packet.fromBytes(readBuffer);

        readQueue.add(packet);
    }

    public NCBasicConnection(SocketChannel channel) {
        this.channel = channel;
        writeBuffer = ByteBuffer.allocate(128);
        readBuffer = ByteBuffer.allocate(128);
        updateReadTime();
        lastWrite = 0;
    }

    public long lastInteraction() {
        return Math.max(lastRead, lastWrite);
    }

    public boolean isTimedOut(long now, long max) {
        return now - lastInteraction() > max;
    }

    private void growWriteBuffer(int newCap) {
        if (newCap > writeBuffer.capacity()) {
            ByteBuffer newWriteBuffer = ByteBuffer.allocate(newCap);

            writeBuffer.flip();
            newWriteBuffer.put(writeBuffer);
            writeBuffer = newWriteBuffer;
        }
    }

    public void sendPacket(NCMessage message) throws ConnectionClosed {
        if (!channel.isOpen())
            throw new ConnectionClosed();
        writeQueue.add(message);
    }

    public BlockingQueue<NCMessage> getReadQueue() {
        return readQueue;
    }

    public long getSessionID() {
        return sessionID;
    }

    public long getClientID() {
        return clientID;
    }

    @Override
    public String toString() {
        return "NCBasicConnection{" +
                "sessionID=" + sessionID +
                ", clientID=" + clientID +
                '}';
    }
}
