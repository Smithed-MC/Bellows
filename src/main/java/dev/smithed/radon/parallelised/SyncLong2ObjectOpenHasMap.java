package dev.smithed.radon.parallelised;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class SyncLong2ObjectOpenHasMap<V> extends Long2ObjectOpenHashMap<V> {


    @Override
    public FastEntrySet<V> long2ObjectEntrySet() {
        synchronized (this) {
            return super.long2ObjectEntrySet();
        }
    }
}
