package nc.net;

import nc.NCConnection;
import nc.exc.ConnectionClosed;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NCConnectionContainer {
    public NCConnection connection;

    public static NCConnection connect(InetSocketAddress server) throws ConnectionClosed {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionClosed();
        }

        SocketChannel channel;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(server);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionClosed();
        }

        try {
            selector.select(1000);
        } catch (IOException e) {
            throw new ConnectionClosed();
        }

        for (SelectionKey key : selector.selectedKeys()) {
            if (key.isConnectable()) {
                channel = (SocketChannel) key.channel();
                try {
                    if (channel.finishConnect()) {
                        NCConnection conn = new NCConnection(channel);
                        selector.close();
                        return conn;
                    }
                } catch (IOException e) {
                    throw new ConnectionClosed();
                }
            }

        }
        selector.selectedKeys().clear();
        throw new ConnectionClosed();
    }
}
