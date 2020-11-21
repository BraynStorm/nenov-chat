package nc;

import nc.exc.PacketCorruptionException;
import nc.message.ClientJoinRoom;

import java.util.HashSet;
import java.util.Set;

public class NCRoom {
    private String name;
    private Set<NCClient> clients;

    public NCRoom(String name) {
        this.name = name;
        clients = new HashSet<>();
    }

    public void clientJoin(NCClient client) throws PacketCorruptionException {
        ClientJoinRoom packet = new ClientJoinRoom(client.getClientID(), "Unnamed");
        for (NCClient alreadyInTheRoom : clients) {
            alreadyInTheRoom.sendPacket(packet);
        }

        clients.add(client);
    }


}
