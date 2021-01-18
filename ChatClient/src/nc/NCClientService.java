package nc;

import nc.exc.ConnectionClosed;
import nc.message.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class NCClientService implements NCMessageVisitor<NCConnection> {
    private final InetSocketAddress address = new InetSocketAddress("213.91.183.197", 5511);

    private State state = State.NOT_CONNECTED;
    private NCConnection connection;
    private List<NCFriend> friendList = new ArrayList<>();

    public List<NCFriend> getFriendList() {
        return friendList;
    }

    public long sessionID() throws ConnectionClosed {
        if (!isConnected())
            throw new ConnectionClosed();
        return connection.sessionID;
    }

    public enum State {
        NOT_CONNECTED,
        BOUND,
        CONNECTED,
        AUTHENTICATED
    }

    public boolean isConnected() {
        return connection != null && state == State.CONNECTED;
    }

    public boolean isBound() {
        return connection != null && state == State.BOUND;
    }

    public boolean isAuthenticated() {
        return connection != null && state == State.AUTHENTICATED;
    }

    public State getState() {
        return state;
    }

    public void networkTick() {
        try {
            tick();
        } catch (InterruptedException e) {
            // Eat
        }
    }

    public boolean send(NCMessage message) throws ConnectionClosed {
        boolean doSend = true;

        if (message.type() == PacketType.AUTHENTICATE) {
            doSend = (state == State.CONNECTED);
        } else if (message.type() == PacketType.REGISTER) {
            doSend = (state == State.CONNECTED);
        }

        if (doSend)
            connection.sendPacket(message);
        return doSend;
    }

    private void tick() throws InterruptedException {
        if (state != State.NOT_CONNECTED) {
            if (connection.isTimedOut(System.nanoTime(), 5_000_000_000L)) {
                try {
                    connection.sendPacket(new Ping());
                    System.out.println("Pinging");
                } catch (ConnectionClosed connectionClosed) {
                    state = State.NOT_CONNECTED;
                    connection = null;
                    friendList.clear();
                }
            }
        }

        NCMessage packet;

        switch (state) {
            case NOT_CONNECTED:
                reconnect();
                break;
            case BOUND:
                // await CONNECTION_SUCCESSFUL
                packet = connection.getReadQueue().poll();

                if (packet != null && packet.type() == PacketType.CONNECT_SUCCESSFUL) {
                    connection.sessionID = ((ConnectSuccessful) packet).sessionID;
                    state = State.CONNECTED;
                }
                break;
            case CONNECTED:
                // await AUTHENTICATION_STATUS
                packet = connection.getReadQueue().poll();
                if (packet != null) {
                    if (packet.type() == PacketType.AUTHENTICATION_STATUS) {
                        connection.clientID = ((AuthenticationStatus) packet).clientID;

                        if (connection.clientID == -1) {
                            System.out.println("Login failed.");
                        } else {
                            System.out.println("Login successful.");
                            state = State.AUTHENTICATED;
                        }
                    } else if (packet.type() == PacketType.REGISTER_STATUS) {
                        connection.clientID = ((RegisterStatus) packet).clientID;

                        if (connection.clientID == -1) {
                            System.out.println("Register failed.");
                        } else {
                            System.out.println("Register successful.");
                            state = State.AUTHENTICATED;
                        }
                    }
                }
                break;
            case AUTHENTICATED:
                packet = connection.getReadQueue().poll();
                if (packet != null)
                    try {
                        NCMessageVisitor.visit(this, connection, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                break;
        }
    }

    private void reconnect() {
        try {
            connection = NCConnectionContainer.connect(address, 25);
            state = State.BOUND;
        } catch (ConnectionClosed connectionClosed) {
            System.out.println("Couldn't connect");
            connection = null;
            state = State.NOT_CONNECTED;
        }
    }

    @Override
    public void onClientUpdateFriendList(NCConnection client, ClientUpdateFriendList packet) throws Exception {
        for (int i = 0; i < packet.status.length; ++i) {
            long friendID = packet.friends[i];
            ClientUpdateFriendList.Status status = packet.getStatus(i);

            friendList.add(new NCFriend(friendID));
        }
    }

    @Override
    public void onClientResponseClientName(NCConnection client, ClientResponseClientName packet) throws Exception {

    }
}
