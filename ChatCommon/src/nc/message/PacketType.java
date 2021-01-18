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
    CLIENT_UPDATE_FRIEND_LIST,
}
