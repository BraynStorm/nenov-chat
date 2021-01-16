package nc;

import nc.exc.ConnectionClosed;
import nc.exc.PacketCorruptionException;
import nc.message.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

/**
 *
 */
public class NCBasicConnection {
    private SocketChannel channel;
    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;
    private final Queue<NCMessage> writeQueue = new ArrayDeque<>();
    private final Queue<NCMessage> readQueue = new ArrayDeque<>();
    private long lastRead;
    private long lastWrite;

    protected long sessionID;
    protected long clientID;

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
                    break;
                }
            } else
                break;
        }
        readBuffer.compact();
    }

    void processWritePackets() {
        if (!writeBuffer.hasRemaining()) return;

        synchronized (writeQueue) {
            while (writeQueue.peek() != null && writeQueue.peek().maximumSize() <= writeBuffer.remaining()) {
                NCMessage message = writeQueue.peek();
                assert message != null;
                growWriteBuffer(message.maximumSize());
                if (message.toBytes(writeBuffer))
                    writeQueue.remove();
                else
                    break;
            }
        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private void processReadPacket(short packetID) throws IOException {
        NCMessage packet;

        switch (PacketType.values()[packetID]) {
            case PING:
                packet = new Ping();
                break;
            case CLIENT_AUTHENTICATE:
                packet = new ClientAuthenticate();
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
            case CLIENT_AUTHENTICATION_STATUS:
                packet = new ClientAuthenticationStatus();
                break;
            default:
                throw new PacketCorruptionException();
        }

        packet.fromBytes(readBuffer);

        synchronized (readQueue) {
            readQueue.add(packet);
        }
    }

    public NCBasicConnection(SocketChannel channel) {
        this.channel = channel;
        writeBuffer = ByteBuffer.allocate(128);
        readBuffer = ByteBuffer.allocate(128);
        lastRead = System.nanoTime();
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
        synchronized (writeQueue) {
            writeQueue.add(message);
        }
    }

    public Queue<NCMessage> getReadQueue() {
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
