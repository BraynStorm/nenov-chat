package nc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NCFriendsList {
    private Map<Long, List<Long>> friends;

    NCFriendsList() {
        friends = new HashMap<>();
    }

    List<Long> FriendsOf(long clientID) {
        return friends.getOrDefault(clientID, new ArrayList<>());
    }

}
