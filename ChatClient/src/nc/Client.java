package nc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client {
    private Selector selector;
    private SocketChannel channel;

    private boolean connected;

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    public Client(InetSocketAddress server) throws IOException {
        selector = Selector.open();
        channel = SocketChannel.open(server);
        channel.configureBlocking(false);

        connected = false;

        readBuffer = ByteBuffer.allocate(128);
        writeBuffer = ByteBuffer.allocate(128);
    }

    public void listen() {
        try {
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    channel = (SocketChannel) key.channel();
                    if (channel.finishConnect())
                        connected = true;
                }
                if (key.isReadable()) {
                    read(key);
                }
                if (key.isWritable()) {
                    write(key);
                }

            }
            selector.selectedKeys().clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connected = false;
    }

    private void read(SelectionKey key) {

    }

    private void write(SelectionKey key) {

    }

    public static void main(String[] args) {

        try {
            Client c = new Client(new InetSocketAddress("localhost", 5511));
            c.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
