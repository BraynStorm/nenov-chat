package nc;

import nc.exc.PortTakenException;
import sun.security.ssl.SSLEngineImpl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NCServer {
    private Selector acceptSelector;
    private Selector readSelector;
    private Selector writeSelector;
    private ServerSocketChannel serverChannel;

    private Map<SelectableChannel, NCClient> clients = new HashMap<>();
    private Set<NCRoom> rooms = new HashSet<>();

    public NCServer(int port) throws PortTakenException, IOException {
        try {
            acceptSelector = Selector.open();
            readSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            System.err.println("[NCServer] Can't create Selector!");
            throw e;
        }

        serverChannel = ServerSocketChannel.open();
        try {
            serverChannel.bind(new InetSocketAddress(port));
            System.out.printf("[NCServer] Listening on port %d\n", port);
        } catch (IOException e) {
            System.err.println("[NCServer] Port is already taken!");
            throw new PortTakenException();
        }

        try {
            serverChannel.configureBlocking(false);
        } catch (IOException e) {
            System.err.println("[NCServer] Non-blocking channels are not supported!");
            throw e;
        }

    }

    public void listen() {
        try {
            serverChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

        while (true) {
            acceptClients();
            readClients();
            processClients();
            writeClients();

            removeTimedOutClients();
            removeDisconnectedClients();
        }
    }

    private void acceptClients() {
        try {
            acceptSelector.select(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (SelectionKey key : acceptSelector.selectedKeys()) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                acceptClient(clientChannel);
            } catch (IOException e) {
                System.out.println("Failed to accept client.");
                e.printStackTrace();
            }

        }

        acceptSelector.selectedKeys().clear();
    }

    private void readClients() {
        try {
            readSelector.select(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (SelectionKey key : readSelector.selectedKeys()) {
            readClient(key);
        }
    }

    private void writeClients() {
        try {
            writeSelector.select(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (SelectionKey key : writeSelector.selectedKeys()) {
            writeClient(key);
        }
    }

    private void processClients() {
        clients.values().forEach(NCClient::processReadPackets);
        clients.values().forEach(NCClient::processWritePackets);
    }

    private void removeDisconnectedClients() {
        clients.keySet().removeAll(
                clients.keySet().stream()
                        .filter(channel -> !channel.isOpen())
                        .collect(Collectors.toSet())
        );
    }

    private void removeTimedOutClients() {
        final long now = System.nanoTime();
        final long maxTime = 9_000_000_000L; // 9 sec
        clients.values().removeAll(
                clients.values().stream()
                        .filter(client -> client.isTimedOut(now, maxTime))
                        .collect(Collectors.toSet())
        );
    }

    private void acceptClient(SocketChannel clientChannel) {
        try {
            System.out.printf("[ACCEPT] %s", clientChannel.getRemoteAddress().toString());
        } catch (IOException e) {
            System.err.println("Couldn't retrieve remote address for a client. Disconnecting");
            close(clientChannel);
        }
        clients.put(clientChannel, new NCClient(clientChannel));
    }

    private void close(SelectableChannel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
            // Eats the exception
        }
    }

    private void readClient(SelectionKey key) {
        NCClient c = clients.get(key.channel());
        if (c == null)
            close(key.channel());
        else {
            try {
                c.read();
            } catch (IOException e) {
                // e.printStackTrace();
                close(key.channel());
            }
        }
    }

    private void writeClient(SelectionKey key) {
        NCClient c = clients.get(key.channel());
        if (c == null)
            close(key.channel());
        else {
            try {
                c.write();
            } catch (IOException e) {
                // e.printStackTrace();
                close(key.channel());
            }
        }
    }

    public static void main(String[] args) {
        try {
            NCServer server = new NCServer(5511);
            server.listen();
        } catch (PortTakenException | IOException e) {
            e.printStackTrace();
        }
    }
}
