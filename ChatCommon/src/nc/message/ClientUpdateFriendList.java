package nc.message;

import nc.exc.PacketCorruptionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ClientUpdateFriendList implements NCMessage {
    public long[] friends;

    public ClientUpdateFriendList() {
    }

    public ClientUpdateFriendList(Collection<Long> friends) throws PacketCorruptionException {
        if (friends.size() > maximumFriendsList())
            throw new PacketCorruptionException();

        this.friends = new long[friends.size()];

        int i = 0;
        for (var f : friends)
            this.friends[i++] = f;
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
}
