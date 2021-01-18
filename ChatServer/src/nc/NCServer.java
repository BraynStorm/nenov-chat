package nc;

import nc.exc.ConnectionClosed;
import nc.exc.PortTakenException;
import nc.message.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import java.util.stream.Collectors;

public class NCServer {
    private Selector acceptSelector;
    private Selector readSelector;
    private Selector writeSelector;
    private ServerSocketChannel serverChannel;

    private Map<SelectableChannel, NCConnection> clients = new HashMap<>();
    private Map<SelectableChannel, String> remoteAddresses = new HashMap<>();
    private Set<NCRoom> rooms = new HashSet<>();

    private NCDB database;
    private volatile boolean stop;
    private volatile boolean stopped;

    private static Logger LOG = Logger.getLogger(NCServer.class.getName());

    public NCServer(int port) throws PortTakenException, IOException {
        database = new NCDB();
        database.connect();

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

    public void stop() {
        stop = true;
        while (!stopped) Thread.onSpinWait();
        database.stop();
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

        while (!stop) {
            acceptClients();
            readClients();
            processClients();
            writeClients();

            removeTimedOutClients();
            removeDisconnectedClients();
        }

        stopped = true;
        stop();
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
        clients.values().forEach(client -> {
            client.processReadPackets();
            var readQueue = client.getReadQueue();

            while (!readQueue.isEmpty()) {
                var packet = readQueue.poll();
                switch (packet.type()) {
                    case PING:
                        try {
                            client.sendPacket(new Ping());
                        } catch (ConnectionClosed connectionClosed) {
                            client.close();
                        }
                        break;
                    case CONNECT_SUCCESSFUL:
                        break;
                    case AUTHENTICATE:
                        handlePacket_Authenticate(client, (Authenticate) packet);
                        break;
                    case CLIENT_JOIN_ROOM:
                        break;
                    case CLIENT_SEND_DIRECT_MESSAGE:
                        handlePacket_ClientSendDirectMessage(client, (ClientSentDirectMessage) packet);
                        break;
                    case AUTHENTICATION_STATUS:
                        break;
                    case REGISTER:
                        handlePacket_Register(client, (Register) packet);
                        break;
                }
            }
        });

        clients.values().forEach(NCBasicConnection::processWritePackets);
    }

    private void handlePacket_Authenticate(NCConnection client, Authenticate packet) {
        if (client.clientID == -1) {
            var email = packet.getEmail();
            var password = packet.getPassword();

            long userID = database.findUser(email, password);

            if (userID == -1)
                LOG.info("Authenticate - Failed");
            else
                LOG.info("Authenticate - Success");

            try {
                client.sendPacket(new AuthenticationStatus(userID));
                if (userID != -1)
                    client.sendPacket(new ClientUpdateFriendList());
            } catch (ConnectionClosed ignored) {
                client.close();
            }
        } else {
            LOG.warning("Authenticate received when already authenticated. Culprit: " + client);
            client.close();
        }
    }

    private void handlePacket_ClientSendDirectMessage(NCConnection client, ClientSentDirectMessage packet) {

    }

    private void handlePacket_Register(NCConnection client, Register packet) {
        LOG.info("Register" + client);
        if (client.getClientID() != -1) {
            LOG.warning("Logged-in client tried to Register. Culprit: " + client.toString());
            client.close();
            return;
        }

        client.clientID = database.createUser(packet.getEmail(), packet.getPassword());

        try {
            client.sendPacket(new RegisterStatus(client.clientID));
            if (client.clientID == -1) {
                LOG.info("Register failed. " + client);
            } else {
                LOG.info("Register success. " + client);
            }
        } catch (ConnectionClosed connectionClosed) {
            client.close();
        }
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

        Set<NCConnection> timeout = clients.values().stream()
                .filter(client -> client.isTimedOut(now, maxTime))
                .collect(Collectors.toSet());

        clients.values().removeAll(timeout);

        for (NCConnection connection : timeout) {
            LOG.info("Timeout " + connection.toString());
        }
    }

    private void acceptClient(SocketChannel clientChannel) {
        LOG.info("Accept " + remoteAddresses.get(clientChannel));

        NCConnection connection = new NCConnection(clientChannel);
        try {
            clientChannel.configureBlocking(false);
            clientChannel.register(readSelector, SelectionKey.OP_READ);
            clientChannel.register(writeSelector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            close(clientChannel);
        }

        try {
            connection.sendPacket(new ConnectSuccessful(connection.getSessionID()));
            clients.put(clientChannel, connection);
        } catch (ConnectionClosed connectionClosed) {
            close(clientChannel);
        }
    }

    private void close(SelectableChannel channel) {
        try {
            LOG.info("Close channel " + remoteAddresses.get(channel));
            channel.close();
        } catch (IOException ignored) {
            LOG.info("Exception during channel.close().");
            // Eats the exception
        }
    }

    private void readClient(SelectionKey key) {
        NCBasicConnection c = clients.get(key.channel());
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
        NCBasicConnection c = clients.get(key.channel());
        if (c == null)
            close(key.channel());
        else {
            try {
                c.write();
            } catch (IOException e) {
                LOG.throwing(NCServer.class.getName(), "writeClient", e);
                close(key.channel());
            }
        }
    }

    public static void main(String[] args) {
        LOG.setUseParentHandlers(false);

        Formatter formatter = new Formatter() {
            final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                return df.format(new Date(record.getMillis())) +
                        " | " +
                        record.getLevel().getName() +
                        " | " +
                        record.getLoggerName() +
                        " | " +
                        record.getMessage() +
                        '\n';
            }
        };
        Handler console = new ConsoleHandler();
        console.setFormatter(formatter);
        LOG.addHandler(console);

        try {
            Handler handler = new FileHandler("ncserver.log");
            handler.setFormatter(formatter);
            LOG.addHandler(handler);
        } catch (IOException e) {
            LOG.warning(e.toString());
        }

        try {
            NCServer server = new NCServer(5511);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Stopping.");
                server.stop();
            }));

            server.listen();
        } catch (PortTakenException | IOException e) {
            e.printStackTrace();
        }
    }
}
