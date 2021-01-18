package nc;

public class NCFriend {
    public long id;
    public String name = "";

    public NCFriend(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return Long.toString(id);
    }
}
