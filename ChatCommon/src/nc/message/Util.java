package nc.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
    public static long[] toLongArray(Collection<Long> collection) {
        long[] arr = new long[collection.size()];
        int i = 0;

        for (long element : collection)
            arr[i++] = element;

        return arr;
    }

    public static int[] toIntArray(Collection<Integer> collection) {
        int[] arr = new int[collection.size()];
        int i = 0;

        for (int element : collection)
            arr[i++] = element;

        return arr;
    }

    public static boolean[] toBooleanArray(Collection<Boolean> collection) {
        boolean[] arr = new boolean[collection.size()];
        int i = 0;

        for (boolean element : collection)
            arr[i++] = element;

        return arr;
    }
}
