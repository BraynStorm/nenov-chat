package nc.message;

import nc.exc.PacketCorruptionException;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetUtil {

    private static <T extends NCMessage> Stream<Field> Fields(Class<T> cls) {
        return Arrays.stream(cls.getFields()).sorted(Comparator.comparing(Field::getName));
    }

    public static <T extends NCMessage> int SizeOf(Class<T> cls) {
        int fieldSize = Fields(cls).mapToInt(f -> {
            if (f.getType() == byte[].class) {
                return 4;
            } else if (f.getType() == short.class) {
                return 2;
            } else if (f.getType() == int.class) {
                return 4;
            } else if (f.getType() == long.class) {
                return 8;
            }
            throw new IllegalArgumentException();
        }).sum();

        return 2 + 4 + fieldSize;
    }

    public static <T extends NCMessage> int TotalSizeOf(final T instance) {
        int arraySize = Fields(instance.getClass()).mapToInt(f -> {
            try {
                if (f.getType() != byte[].class)
                    return 0;

                byte[] arr = (byte[]) f.get(instance);
                if (arr == null)
                    return 0;
                else
                    return arr.length;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return 0;
            }
        }).sum();

        return SizeOf(instance.getClass()) + arraySize;
    }

    public static class Read {
        public static void CheckPacketID(ByteBuffer buffer, PacketType packet) throws PacketCorruptionException {
            short netID = buffer.getShort();

            if (netID != (short) packet.ordinal()) {
                throw new PacketCorruptionException();
            }
        }

        public static int Size(ByteBuffer buffer) {
            return buffer.getInt();
        }

        public static void Check(boolean cond) throws PacketCorruptionException {
            if (!cond)
                throw new PacketCorruptionException();
        }

        public static byte[] Bytes(ByteBuffer buffer) throws PacketCorruptionException {
            if (buffer.remaining() < 4)
                throw new PacketCorruptionException();

            int size = buffer.getInt();

            if (size < 0)
                throw new PacketCorruptionException();

            if (buffer.remaining() < size)
                throw new PacketCorruptionException();

            byte[] arr = new byte[size];

            buffer.get(arr);
            return arr;
        }

        public static <T extends NCMessage> void AutoRead(ByteBuffer source, T packet) throws PacketCorruptionException {
            CheckPacketID(source, packet.type());

            int size = Size(source);
            Check(size <= packet.maximumSize());

            for (Field f : Fields(packet.getClass()).collect(Collectors.toList())) {
                try {
                    if (f.getType() == byte[].class) {
                        if (source.remaining() < 4)
                            throw new PacketCorruptionException();

                        int arraySize = source.getInt();
                        if (source.remaining() < arraySize || arraySize < 0)
                            throw new PacketCorruptionException();

                        byte[] bytes = new byte[arraySize];
                        source.get(bytes);
                        f.set(packet, bytes);
                    } else if (f.getType() == short.class) {
                        if (source.remaining() < 2)
                            throw new PacketCorruptionException();

                        f.set(packet, source.getShort());
                    } else if (f.getType() == int.class) {
                        if (source.remaining() < 4)
                            throw new PacketCorruptionException();

                        f.set(packet, source.getInt());
                    } else if (f.getType() == long.class) {
                        if (source.remaining() < 8)
                            throw new PacketCorruptionException();

                        f.set(packet, source.getLong());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public static class Write {
        public static boolean CanFit(ByteBuffer buffer, int totalPacketSize) {
            return buffer.remaining() >= totalPacketSize;
        }

        public static void WriteHeader(ByteBuffer buffer, PacketType type, int size) {
            buffer.putShort((short) type.ordinal());
            buffer.putInt(size);
        }

        public static void WriteBytes(ByteBuffer buffer, byte[] bytes) {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        public static <T extends NCMessage> boolean AutoWrite(ByteBuffer destination, T packet) {
            int size = packet.size();
            boolean canFit = Write.CanFit(destination, size);

            if (canFit) {
                destination.putShort((short) packet.type().ordinal());
                destination.putInt(size);

                for (Field f : Fields(packet.getClass()).collect(Collectors.toList())) {
                    try {
                        if (f.getType() == byte[].class) {
                            byte[] bytes = (byte[]) f.get(packet);
                            destination.putInt(bytes.length);
                            destination.put(bytes);
                        } else if (f.getType() == short.class) {
                            destination.putShort((short) f.get(packet));
                        } else if (f.getType() == int.class) {
                            destination.putInt((int) f.get(packet));
                        } else if (f.getType() == long.class) {
                            destination.putLong((long) f.get(packet));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }

            return canFit;
        }
    }
}

