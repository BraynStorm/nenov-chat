package nc;

import nc.exc.PacketCorruptionException;
import nc.exception.ConnectionClosed;
import nc.message.ClientAuthenticate;
import nc.message.ClientSentDirectMessage;
import nc.message.NCMessage;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client {
    private Selector selector;

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    private long session = 0;

    public Client() throws IOException {
        selector = Selector.open();

        readBuffer = ByteBuffer.allocate(2048);
        writeBuffer = ByteBuffer.allocate(2048);
    }

    public void listen(InetSocketAddress server) throws ConnectionClosed {
        SocketChannel channel;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);

            readBuffer.clear();
            writeBuffer.clear();
            channel.connect(server);
        } catch (IOException e) {
            throw new ConnectionClosed();
        }

        while (true) {
            try {
                selector.select(100);
            } catch (IOException e) {
                throw new ConnectionClosed();
            }

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    channel = (SocketChannel) key.channel();
                    try {
                        if (channel.finishConnect()) {
                            channel.register(selector, SelectionKey.OP_READ);
                            channel.register(selector, SelectionKey.OP_WRITE);
                        }
                    } catch (IOException e) {
                        throw new ConnectionClosed();
                    }
                }

                if (key.isReadable()) {
                    read(key);
                }
                if (key.isWritable()) {
                    write(key);
                }

            }
            selector.selectedKeys().clear();
        }
    }

    private void read(SelectionKey key) throws ConnectionClosed {
        SocketChannel c = (SocketChannel) key.channel();
        try {
            c.read(readBuffer);
        } catch (IOException e) {
            throw new ConnectionClosed();
        }

    }

    private void write(SelectionKey key) throws ConnectionClosed {
        SocketChannel c = (SocketChannel) key.channel();
        try {
            c.write(writeBuffer);
        } catch (IOException e) {
            throw new ConnectionClosed();
        }
    }

    public NCMessage readPacket() {
        if (readBuffer.hasRemaining()) {
            return null;
        } else
            return null;
    }

    public boolean writePacket(NCMessage packet) {
        return packet.toBytes(writeBuffer);
    }


    public static void main(String[] args) {
        Client c;
        try {
            c = new Client();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        InetSocketAddress address = new InetSocketAddress("localhost", 5511);

        while (true) {
            try {
                c.listen(address);
            } catch (ConnectionClosed e) {
                e.printStackTrace();
            }
            System.out.println("[Client] Connection closed. Reconnecting.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
