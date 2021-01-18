package nc.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
    public static long[] toLongArray(Collection<Long> collection) {
        var arr = new long[collection.size()];
        int i = 0;


        for (var element : collection)
            arr[i++] = element;

        return arr;
    }

    public static boolean[] toBooleanArray(Collection<Boolean> collection) {
        var arr = new boolean[collection.size()];
        int i = 0;

        for (var element : collection)
            arr[i++] = element;

        return arr;
    }
}
