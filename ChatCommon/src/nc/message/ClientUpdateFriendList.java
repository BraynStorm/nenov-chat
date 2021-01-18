package nc.message;

import nc.exc.PacketCorruptionException;

import java.util.List;

public class ClientUpdateFriendList extends NCMessage {
    public long[] friends;
    public boolean[] online;

    public ClientUpdateFriendList() {
    }

    public ClientUpdateFriendList(List<Long> friends, List<Boolean> online) throws PacketCorruptionException {
        if (friends.size() > maximumFriendsList())
            throw new PacketCorruptionException();
        if (online.size() > maximumFriendsList())
            throw new PacketCorruptionException();

        this.friends = Util.toLongArray(friends);
        this.online = Util.toBooleanArray(online);
    }

    public int maximumFriendsList() {
        return 512;
    }

    @Override
    public PacketType type() {
        return PacketType.CLIENT_UPDATE_FRIEND_LIST;
    }

    @Override
    public int maximumSize() {
        return fixedSize() + maximumFriendsList() * 8;
    }

    @Override
    public void validatePostRead() throws PacketCorruptionException {
        super.validatePostRead();
    }
}
