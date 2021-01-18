package nc.message;

import nc.NCBasicConnection;

public interface NCMessageVisitor<Conn extends NCBasicConnection> {
    static <Conn extends NCBasicConnection> void visit(NCMessageVisitor<Conn> visitor, Conn client, NCMessage packet) throws Exception {
        switch (packet.type()) {
            case PING:
                visitor.onPing(client, (Ping) packet);
                break;
            case CONNECT_SUCCESSFUL:
                visitor.onConnectSuccessful(client, (ConnectSuccessful) packet);
                break;
            case AUTHENTICATE:
                visitor.onAuthenticate(client, (Authenticate) packet);
                break;
            case CLIENT_JOIN_ROOM:
                visitor.onClientJoinRoom(client, (ClientJoinRoom) packet);
                break;
            case CLIENT_SEND_DIRECT_MESSAGE:
                visitor.onClientSentDirectMessage(client, (ClientSentDirectMessage) packet);
                break;
            case AUTHENTICATION_STATUS:
                visitor.onAuthenticationStatus(client, (AuthenticationStatus) packet);
                break;
            case REGISTER:
                visitor.onRegister(client, (Register) packet);
                break;
            case REGISTER_STATUS:
                visitor.onRegisterStatus(client, (RegisterStatus) packet);
                break;
            case CLIENT_UPDATE_FRIEND_LIST:
                visitor.onClientUpdateFriendList(client, (ClientUpdateFriendList) packet);
                break;
            case CLIENT_USER_CHANGED_STATUS:
                visitor.onClientUserChangedStatus(client, (ClientUserChangedStatus) packet);
                break;
            case CLIENT_REQUEST_CLIENT_NAME:
                visitor.onClientRequestClientName(client, (ClientRequestClientName) packet);
                break;
            case CLIENT_RESPONSE_CLIENT_NAME:
                visitor.onClientResponseClientName(client, (ClientResponseClientName) packet);
                break;
        }
    }

    default void onPing(Conn client, Ping packet) throws Exception {
    }

    default void onAuthenticate(Conn client, Authenticate packet) throws Exception {
    }

    default void onAuthenticationStatus(Conn client, AuthenticationStatus packet) throws Exception {
    }

    default void onClientJoinRoom(Conn client, ClientJoinRoom packet) throws Exception {
    }

    default void onClientSentDirectMessage(Conn client, ClientSentDirectMessage packet) throws Exception {
    }

    default void onClientUpdateFriendList(Conn client, ClientUpdateFriendList packet) throws Exception {
    }

    default void onConnectSuccessful(Conn client, ConnectSuccessful packet) throws Exception {
    }

    default void onRegister(Conn client, Register packet) throws Exception {
    }

    default void onRegisterStatus(Conn client, RegisterStatus packet) throws Exception {
    }

    default void onClientUserChangedStatus(Conn client, ClientUserChangedStatus packet) throws Exception {
    }

    default void onClientRequestClientName(Conn client, ClientRequestClientName packet) throws Exception {
    }

    default void onClientResponseClientName(Conn client, ClientResponseClientName packet) throws Exception {
    }

}
