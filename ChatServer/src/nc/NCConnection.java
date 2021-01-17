package nc;

import nc.message.NCMessage;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Random;

public class NCConnection extends NCBasicConnection {
    public NCConnection(SocketChannel channel) {
        super(channel);

        Random rng = new Random();
        sessionID = rng.nextLong();
        clientID = -1;
    }

}
