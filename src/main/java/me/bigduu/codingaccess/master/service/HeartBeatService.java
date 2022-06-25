package me.bigduu.codingaccess.master.service;

import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import me.bigduu.codingaccess.common.domain.node.AbstractNode;
import me.bigduu.codingaccess.slave.domain.SlaveNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClientRequest;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class HeartBeatService {
    private final Set<SlaveNode> slaveNodeList;
    private final WebClient webClient;

    private final AtomicBoolean flag = new AtomicBoolean(false);

    public HeartBeatService(WebClient webClient) {
        this.slaveNodeList = new ConcurrentSkipListSet<>();
        this.webClient = webClient;
    }

    @Scheduled(cron = "* * * * * *")
    public void keepAlive() {
        if (!slaveNodeList.isEmpty()) {
            flag.set(true);
            Flux.fromIterable(slaveNodeList)
                    .parallel(2)
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(it ->
                            webClient.get()
                                    .uri("http://localhost:" + it.getPort() + "/keep-alive")
                                    .httpRequest(httpRequest -> {
                                        final HttpClientRequest nativeRequest = httpRequest.getNativeRequest();
                                        nativeRequest.responseTimeout(Duration.ofMillis(500));
                                    })
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .map(response -> Tuple.of(it, response))
                                    .onErrorResume(e -> Mono.just(Tuple.of(it, "error")))
                    )
                    .filter(it -> !"error".equals(it._2))
                    .doOnNext(it -> it._1.pingAlive())
                    .doOnNext(it -> {
                        if (flag.get()) {
                            synchronized (flag) {
                                if (flag.get()) {
                                    flag.set(false);
                                    webClient.post()
                                            .uri("http://localhost:" + it._1.getPort() + "/write")
                                            .httpRequest(httpRequest -> {
                                                final HttpClientRequest nativeRequest = httpRequest.getNativeRequest();
                                                nativeRequest.responseTimeout(Duration.ofMillis(500));
                                            })
                                            .retrieve()
                                            .bodyToMono(String.class)
                                            .filter("ERROR"::equalsIgnoreCase)
                                            .doOnNext(item -> log.error("Write file error in port {}", it._1.getPort()))
                                            .onErrorResume(e -> {
                                                log.error("Request write file error in port {}", it._1.getPort(), e);
                                                return Mono.empty();
                                            })
                                            .subscribe();
                                }
                            }
                        }
                    })
                    .sequential()
                    .subscribeOn(Schedulers.single())
                    .collectList()
                    .subscribe(ignore -> slaveNodeList.removeIf(AbstractNode::isNotHealth));
        }
    }

    public void addSlaveNode(Integer port) {
        final var slaveNode = new SlaveNode(port, 800L, TimeUnit.MILLISECONDS);
        this.slaveNodeList.add(slaveNode);
    }
}
