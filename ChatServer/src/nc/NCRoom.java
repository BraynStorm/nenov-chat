package nc;

import nc.exc.ConnectionClosed;
import nc.exc.PacketCorruptionException;
import nc.message.ClientJoinRoom;

import java.util.HashSet;
import java.util.Set;

public class NCRoom {
    private String name;
    private Set<NCConnection> clients;

    public NCRoom(String name) {
        this.name = name;
        clients = new HashSet<>();
    }

    public void clientJoin(NCConnection client) throws PacketCorruptionException {
        ClientJoinRoom packet = new ClientJoinRoom(client.getClientID(), "Unnamed");
        for (NCConnection alreadyInTheRoom : clients) {
            try {
                alreadyInTheRoom.sendPacket(packet);
            } catch (ConnectionClosed connectionClosed) {
                alreadyInTheRoom.close();
            }
        }

        clients.add(client);
    }


}
