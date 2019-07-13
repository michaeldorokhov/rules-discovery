package ee.ut.mykhailodorokhov.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListHelper {

    public static Integer minInteger(List<Integer> list) {
        return list.stream().min(Comparator.comparing(Integer::valueOf)).get();
    }

    public static Integer maxInteger(List<Integer> list) {
        return list.stream().max(Comparator.comparing(Integer::valueOf)).get();
    }

    public static Double maxDouble (List<Double> list) {
        return list.stream().max(Comparator.comparing(Double::valueOf)).get();
    }

    public static Double minDouble (List<Double> list) {
        return list.stream().min(Comparator.comparing(Double::valueOf)).get();
    }

    public static String[] sliceArray(String[] arrayToSlice, int startIndex, int endIndex) {
        ArrayList<String> slicedArray = new ArrayList<>();

        for (int i = 0; i < arrayToSlice.length; i++) {
            if (startIndex <= i && i <= endIndex)
                slicedArray.add(arrayToSlice[i]);
        }

        return slicedArray.toArray(new String[slicedArray.size()]);
    }
}
