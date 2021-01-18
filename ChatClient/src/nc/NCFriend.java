package nc;

import java.util.ArrayList;
import java.util.List;

public class NCFriend {
    public long id;
    public String name = "";
    public List<String> messages = new ArrayList<String>();
    public boolean online;


    public NCFriend(long id) {
        this.id = id;
    }

    public NCFriend(long id, String name, boolean online) {
        this.id = id;
        this.name = name;
        this.online = online;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    @Override
    public String toString() {
        return name;
    }
}
