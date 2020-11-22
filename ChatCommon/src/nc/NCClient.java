package nc;

import nc.exc.PacketCorruptionException;
import nc.message.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class NCClient {
    private SocketChannel channel;
    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;
    private Queue<NCMessage> writeQueue;
    private Queue<NCMessage> readQueue;
    private long lastRead;
    private long lastWrite;

    private long sessionID;
    private long clientID;

    private void updateReadTime() {
        lastRead = System.nanoTime();
    }

    private void updateWriteTime() {
        lastRead = System.nanoTime();
    }

    void read() throws IOException {
        if (readBuffer.hasRemaining()) {
            int read = channel.read(readBuffer);
            if (read == 0) {
                updateReadTime();
            }
        }
    }

    void write() throws IOException {
        if (writeBuffer.limit() > 0) {
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
                processReadPacket(packetID);
            } else
                break;
        }
        readBuffer.compact();
    }

    void processWritePackets() {
        if (writeBuffer.hasRemaining()) {
            while (writeQueue.peek() != null && writeQueue.peek().maximumSize() <= writeBuffer.remaining()) {
                NCMessage message = writeQueue.peek();
                assert message != null;
                if (message.toBytes(writeBuffer))
                    writeQueue.remove();
                else
                    break;
            }
        }
    }

    private void close() {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private void processReadPacket(short packetID) {
        try {
            NCMessage packet;

            switch (PacketType.values()[packetID]) {
                case PING:
                    packet = new Ping();
                    break;
                case CLIENT_AUTHENTICATE:
                    packet = new ClientAuthenticate();
                    break;
                default:
                    throw new PacketCorruptionException();
            }

            packet.fromBytes(readBuffer);
            readQueue.add(packet);
        } catch (IOException e) {
            close();
        }
    }

    public NCClient(SocketChannel channel) {
        this.channel = channel;
        writeBuffer = ByteBuffer.allocate(128);
        readBuffer = ByteBuffer.allocate(128);
        lastRead = System.nanoTime();
        lastWrite = 0;

        writeQueue = new ArrayDeque<>();
        readQueue = new ArrayDeque<>();


        Random rng = new Random();
        sessionID = rng.nextLong();
        clientID = 0;

        sendPacket(new ConnectSuccessful(sessionID));
    }

    public long lastInteraction() {
        return Math.max(lastRead, lastWrite);
    }

    public String clientAddress() {
        try {
            return channel.getRemoteAddress().toString();
        } catch (IOException e) {
            close();
            return "<unknown>";
        }
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

    public void sendPacket(NCMessage message) {
        growWriteBuffer(message.maximumSize());

        if (!message.toBytes(writeBuffer)) {
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
}
