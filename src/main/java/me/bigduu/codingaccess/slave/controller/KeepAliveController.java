package me.bigduu.codingaccess.slave.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/keep-alive")
public class KeepAliveController {
    @GetMapping
    public Mono<String> keepAlive() {
        log.info("enter keep alive");
        return Mono.just("OK");
    }
}
