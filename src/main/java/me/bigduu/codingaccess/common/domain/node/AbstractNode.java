package me.bigduu.codingaccess.common.domain.node;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AbstractNode implements Node {
    protected final Integer port;
    private final Long liveThreshold;
    protected final AtomicLong lastLiveTime = new AtomicLong();

    protected AbstractNode(Integer port, Long duration, TimeUnit timeUnit) {
        this(port, timeUnit.toMillis(duration));
    }

    protected AbstractNode(Integer port, Long liveThreshold) {
        this.port = port;
        this.liveThreshold = liveThreshold;
    }

    public void pingAlive() {
        lastLiveTime.lazySet(System.currentTimeMillis());
    }

    @Override
    public Integer getPort() {
        return this.port;
    }

    @Override
    public Boolean isNotHealth() {
        final var bool = System.currentTimeMillis() - lastLiveTime.get() > liveThreshold;
        if (bool) {
            log.info("Port {} will be removed", port);
        }
        return bool;
    }

}
