package floatingmuseum.sonic.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Floatingmuseum on 2017/3/20.
 */

public class ListUtil {

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    /**
     * Will return null when new page doesn't have data
     */
    public static List subList(List list, int page, int limit) {
        int fromIndex = (page - 1) * limit;
        int toIndex = page * limit;

        if (fromIndex >= list.size()) {
            return null;
        } else if (list.size() - fromIndex == 1) {
            List oneElementList = new ArrayList<>();
            oneElementList.add(list.get(list.size() - 1));
            return oneElementList;
        } else if (toIndex > list.size() - 1) {
            toIndex = list.size() - 1;
        }
        return list.subList(fromIndex, toIndex);
    }
}
