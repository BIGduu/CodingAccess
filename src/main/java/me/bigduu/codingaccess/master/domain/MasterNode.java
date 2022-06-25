package me.bigduu.codingaccess.master.domain;

import me.bigduu.codingaccess.common.domain.node.AbstractNode;
import me.bigduu.codingaccess.common.domain.node.Node;

import java.util.concurrent.TimeUnit;

public class MasterNode extends AbstractNode implements Node {

    public MasterNode(Integer port, Long duration, TimeUnit timeUnit) {
        super(port, duration, timeUnit);
    }
}
