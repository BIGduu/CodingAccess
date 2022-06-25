package me.bigduu.codingaccess.master.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bigduu.codingaccess.common.collections.LazyTimeOutSet;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final Set<Integer> portsInUse = new ConcurrentSkipListSet<>();
    private final HeartBeatService heartBeatService;

    private final Set<Integer> availablePorts = Stream.iterate(8081, it -> it + 1)
            .limit(5000)
            .collect(Collectors.toSet());

    private final LazyTimeOutSet<Integer> portsMayInUse = new LazyTimeOutSet<>(TimeUnit.SECONDS, 30L);

    private final Object lock = new Object();

    public Flux<Integer> getPortInUse() {
        return Flux.fromIterable(portsInUse);
    }

    public Mono<Integer> getAnAvailablePorts() {
        return Mono.fromSupplier(this.getAnAvailablePortsSupplier());
    }


    public Mono<Boolean> register(@PathVariable Integer port) {
        log.info("port {} has been registered", port);
        return Mono.justOrEmpty(portsInUse.add(port))
                .doOnNext(it -> heartBeatService.addSlaveNode(port))
                .onErrorResume(e -> Mono.just(false));
    }

    public Mono<Boolean> unregister(@PathVariable Integer port) {
        return Mono.justOrEmpty(portsInUse.remove(port));
    }


    @SuppressWarnings("java:S119")
    private Supplier<Integer> getAnAvailablePortsSupplier() {
        return () -> {
            synchronized (lock) {
                final var set = new HashSet<>(availablePorts);
                set.removeAll(portsInUse);
                set.removeAll(portsMayInUse.getAll());
                if (set.iterator().hasNext()) {
                    final var next = set.iterator().next();
                    log.info("Alloc port {}", next);
                    return portsMayInUse.put(next);
                } else {
                    log.warn("The alloc port use up complete!");
                    return -1;
                }
            }
        };
    }

}
