package nc;

import nc.message.NCMessage;

import java.util.function.Function;

public interface NCPacketHandler extends Function<NCMessage, Void> {
}
