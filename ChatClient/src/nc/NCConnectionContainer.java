package nc;

import nc.NCConnection;
import nc.exc.ConnectionClosed;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NCConnectionContainer {
    public static NCConnection connect(InetSocketAddress server, int timeout) throws ConnectionClosed {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionClosed();
        }

        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(server);
        } catch (IOException e) {
            try {
                selector.close();
                if(channel != null)
                    channel.close();
            } catch (IOException ignored) {
            }
            throw new ConnectionClosed();
        }

        try {
            selector.select(timeout);
        } catch (IOException e) {
            try {
                channel.close();
                selector.close();
            } catch (IOException ignored) {
            }
            throw new ConnectionClosed();
        }

        for (SelectionKey key : selector.selectedKeys()) {
            if (key.isConnectable()) {
                var keyChannel = (SocketChannel) key.channel();
                try {
                    if (keyChannel.finishConnect()) {
                        NCConnection conn = new NCConnection(keyChannel);
                        selector.close();
                        System.out.println("Connection established");
                        return conn;
                    }
                } catch (IOException e) {
                    try {
                        channel.close();
                        selector.close();
                    } catch (IOException ignored) {
                    }
                    throw new ConnectionClosed();
                }
            }

        }
        selector.selectedKeys().clear();
        throw new ConnectionClosed();
    }
}
