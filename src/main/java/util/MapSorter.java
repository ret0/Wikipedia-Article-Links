package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generic Map Sorter
 * @param <K> Key Type
 * @param <V> Value Type
 */
public final class MapSorter<K, V extends Comparable<? super V>> {

    /**
     * Returns a new Map, sorted by value
     */
    public Map<K, V> sortByValue(final Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<K, V>>() {
            @Override
            public int compare(final Entry<K, V> arg0,
                               final Entry<K, V> arg1) {
                return arg1.getValue().compareTo(arg0.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
