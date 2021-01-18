package nc;

import java.util.ArrayList;
import java.util.List;

public class NCFriend {
    public long id;
    public String name = "";
    public boolean online;
    public List<NCChatMessage> messages = new ArrayList<>();

    public NCFriend(long id) {
        this.id = id;
    }

    public NCFriend(long id, String name, boolean online) {
        this.id = id;
        this.name = name;
        this.online = online;
    }

    @Override
    public String toString() {
        return name;
    }
}
