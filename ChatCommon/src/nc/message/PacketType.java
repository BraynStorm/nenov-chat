package nc.message;

public enum PacketType {
    PING,
    CONNECT_SUCCESSFUL,
    AUTHENTICATE,
    CLIENT_JOIN_ROOM,
    CLIENT_SEND_DIRECT_MESSAGE,
    AUTHENTICATION_STATUS,
    REGISTER,
    REGISTER_STATUS,
    CLIENT_USER_CHANGED_STATUS,
    CLIENT_REQUEST_CLIENT_NAME,
    CLIENT_RESPONSE_CLIENT_NAME,
    CLIENT_ADD_FRIEND,
    CLIENT_REMOVE_FRIEND,
}
