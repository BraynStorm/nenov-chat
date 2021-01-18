package nc.message;

import nc.exc.PacketCorruptionException;

import java.util.List;

public class ClientUpdateFriendList extends NCMessage {
    public long[] friends;
    public int[] status;

    public static enum Status {
        Offline,
        Online
    }

    public ClientUpdateFriendList() {
    }

    public ClientUpdateFriendList(List<Long> friends, List<Status> status) throws PacketCorruptionException {
        if (friends.size() > maximumFriendsList())
            throw new PacketCorruptionException();
        if (status.size() > maximumFriendsList())
            throw new PacketCorruptionException();

        assert status.size() == friends.size();

        this.friends = Util.toLongArray(friends);

        this.status = new int[status.size()];
        for (int i = 0; i < status.size(); ++i)
            this.status[i] = status.get(i).ordinal();

    }

    public Status getStatus(int index) {
        return Status.values()[status[index]];
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

        // TODO validate - See constructor.
    }
}
