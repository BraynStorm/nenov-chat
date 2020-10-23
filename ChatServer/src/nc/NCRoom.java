package nc;

import java.util.HashSet;
import java.util.Set;

public class NCRoom {
    private String name;
    private Set<NCClient> clients;

    public NCRoom(String name) {
        this.name = name;
        clients = new HashSet<>();
    }

    public void clientJoin(NCClient client) {
        for(NCClient alreadyInTheRoom : clients){
            alreadyInTheRoom.sendPacket();
        }


        clients.add(client);
    }


}
