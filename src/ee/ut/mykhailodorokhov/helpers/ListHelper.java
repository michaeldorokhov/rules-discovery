package ee.ut.mykhailodorokhov.helpers;

import java.util.Comparator;
import java.util.List;

public class ListHelper {

    public static Integer min (List<Integer> list) {
        return list.stream().min(Comparator.comparing(Integer::valueOf)).get();
    }

}
