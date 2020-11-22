package nc;

import nc.exc.PortTakenException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import java.util.stream.Collectors;

public class NCServer {
    private Selector acceptSelector;
    private Selector readSelector;
    private Selector writeSelector;
    private ServerSocketChannel serverChannel;

    private Map<SelectableChannel, NCClient> clients = new HashMap<>();
    private Map<SelectableChannel, String> remoteAddresses = new HashMap<>();
    private Set<NCRoom> rooms = new HashSet<>();

    private static Logger LOG = Logger.getLogger(NCServer.class.getName());

    public NCServer(int port) throws PortTakenException, IOException {

        try {
            acceptSelector = Selector.open();
            readSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            LOG.severe("Can't create selector");
            throw e;
        }

        serverChannel = ServerSocketChannel.open();
        try {
            serverChannel.bind(new InetSocketAddress(port));

            LOG.info("Bound on port " + port);
        } catch (IOException e) {
            LOG.severe("Port is already taken!");
            throw new PortTakenException();
        }

        try {
            serverChannel.configureBlocking(false);
        } catch (IOException e) {
            LOG.severe("Non-blocking channels are not supported!");
            throw e;
        }

    }

    public void listen() {
        LOG.info("Listening");
        clients.clear();
        remoteAddresses.clear();

        try {
            serverChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            LOG.throwing(getClass().getName(), "listen", e);
            return;
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
                remoteAddresses.put(clientChannel, clientChannel.getRemoteAddress().toString());
                acceptClient(clientChannel);
            } catch (IOException e) {
                LOG.info("Failed to accept client.");
                close(key.channel());
            }
        }

        acceptSelector.selectedKeys().clear();
    }

    private void readClients() {
        try {
            readSelector.select(1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (SelectionKey key : readSelector.selectedKeys()) {
            readClient(key);
        }
        readSelector.selectedKeys().clear();
    }

    private void writeClients() {
        try {
            writeSelector.select(1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (SelectionKey key : writeSelector.selectedKeys()) {
            writeClient(key);
        }
        writeSelector.selectedKeys().clear();
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
            System.out.printf("[NCServer] Accept %s\n", clientChannel.getRemoteAddress().toString());
        } catch (IOException e) {
            System.err.println("[NCServer] Couldn't retrieve remote address for a client. Disconnecting");
            close(clientChannel);
        }
        clients.put(clientChannel, new NCClient(clientChannel));
    }

    private void close(SelectableChannel channel) {
        try {
            LOG.info("Close channel " + remoteAddresses.get(channel));
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
                NCServer.LOG.throwing(NCServer.class.getName(), "writeClient", e);
                close(key.channel());
            }
        }
    }

    public static void main(String[] args) {
        NCServer.LOG.setUseParentHandlers(false);

        Formatter formatter = new Formatter() {
            final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss Z");

            @Override
            public String format(LogRecord record) {
                return df.format(new Date(record.getMillis())) +
                        " | " +
                        record.getLevel().getName() +
                        " | " +
                        record.getMessage() +
                        '\n';
            }
        };
        Handler console = new ConsoleHandler();
        console.setFormatter(formatter);
        NCServer.LOG.addHandler(console);

        try {
            Handler handler = new FileHandler("ncserver.log");
            handler.setFormatter(formatter);
            NCServer.LOG.addHandler(handler);
        } catch (IOException e) {
            NCServer.LOG.warning(e.toString());
        }

        try {
            NCServer server = new NCServer(5511);
            server.listen();
        } catch (PortTakenException | IOException e) {
            e.printStackTrace();
        }
    }
}
