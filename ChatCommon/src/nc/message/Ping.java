package nc.message;

import java.io.Serializable;

public class Ping extends NCMessage implements Serializable {
    public long time = 0;

    @Override
    public int maximumSize() {
        return NetUtil.SizeOf(getClass());
    }

    @Override
    public PacketType type() {
        return PacketType.PING;
    }
}
