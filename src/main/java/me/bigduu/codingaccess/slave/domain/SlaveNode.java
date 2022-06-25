package me.bigduu.codingaccess.slave.domain;

import lombok.EqualsAndHashCode;
import me.bigduu.codingaccess.common.domain.node.AbstractNode;
import me.bigduu.codingaccess.common.domain.node.Node;

import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
public class SlaveNode extends AbstractNode implements Node, Comparable<SlaveNode> {

    public SlaveNode(Integer port, Long duration, TimeUnit timeUnit) {
        super(port, duration, timeUnit);
    }

    @Override
    public int compareTo(SlaveNode o) {
        return this.getPort().compareTo(o.getPort());
    }
}
