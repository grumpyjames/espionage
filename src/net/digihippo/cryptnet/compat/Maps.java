package net.digihippo.cryptnet.compat;

import java.util.Map;

public final class Maps
{
    public static <K, V> V computeIfAbsent(
        Map<K, V> map,
        K key,
        Function<K, V> computer
    ) {
        V existing = map.get(key);

        if (existing != null) {
            return existing;
        } else {
            V newValue = computer.apply(key);
            map.put(key, newValue);
            return newValue;
        }
    }
}
