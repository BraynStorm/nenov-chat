package nc.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface NCMessage {
    public static final Charset charset = Charset.forName("UTF-8");

    boolean toBytes(ByteBuffer destination);

    void fromBytes(ByteBuffer source) throws IOException;

    int maximumSize();
}
