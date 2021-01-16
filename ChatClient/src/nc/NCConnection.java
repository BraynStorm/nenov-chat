package nc;

import nc.exc.ConnectionClosed;
import nc.message.NCMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NCConnection extends NCBasicConnection {
    private Selector selector;
    private Thread thread;

    public NCConnection(SocketChannel channel) throws ConnectionClosed {
        super(channel);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            throw new ConnectionClosed();
        }


        thread = new Thread(this::thread);
        thread.start();
    }

    private void thread() {
        while (true) {
            try {
                selector.select(100);
            } catch (IOException e) {
                e.printStackTrace();
            }

            processWritePackets();

            Iterator i = selector.selectedKeys().iterator();
            try {
                while (i.hasNext()) {
                    SelectionKey key = (SelectionKey) i.next();
                    if (key.isReadable()) {
                        read();
                    }

                    if (key.isWritable()) {
                        write();
                    }

                    i.remove();
                }
            } catch (IOException ignored) {
                // Eat the exception
            }

            processReadPackets();
        }
    }

    public void setClientID(long clientID) {
        this.clientID = clientID;
    }

    @Override
    public void sendPacket(NCMessage message) throws ConnectionClosed {
        if (thread.isAlive())
            super.sendPacket(message);
        else
            throw new ConnectionClosed();
    }
}
