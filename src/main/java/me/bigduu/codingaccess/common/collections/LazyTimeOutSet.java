package me.bigduu.codingaccess.common.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LazyTimeOutSet<T> {

    private final Long deferTime;

    private final Set<Entry<T>> internalSet = new HashSet<>();


    public LazyTimeOutSet(TimeUnit timeoutUnit, Long timeout) {
        this.deferTime = timeoutUnit.toMillis(timeout);
    }

    // IT -> internal type
    @SuppressWarnings("java:S119")
    private record Entry<IT>(Long timeoutTimestamp, IT data) {
    }

    public T put(T data) {
        internalSet.add(new Entry<>(System.currentTimeMillis() + deferTime, data));
        return data;
    }

    public Set<T> getAll() {
        final var l = System.currentTimeMillis();
        internalSet.removeIf(it -> it.timeoutTimestamp < l);
        return internalSet.stream().map(it -> it.data).collect(Collectors.toSet());
    }
}

