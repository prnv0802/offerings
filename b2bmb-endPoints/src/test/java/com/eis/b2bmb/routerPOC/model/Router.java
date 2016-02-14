package com.eis.b2bmb.routerPOC.model;

import com.eis.b2bmb.routerPOC.service.HiearchicalDispatcherImpl;

import java.util.Collection;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 2:50 PM
 */
public class Router extends Node {
    protected Registry registry = new Registry();
    protected Dispatcher dispatcher = new HiearchicalDispatcherImpl(this);
    protected Collector collector;
    protected Router parent = null;



    public Collection<Node> getRegisteredNodes() {
        return registry.getNodeMap().values();
    }

    public void register(Node node) {
        registry.getNodeMap().put(node.getAddress(), node);
    }

    public Node getNode(Address address) {
        return registry.getNodeMap().get(address);
    }

    public Address getAddressForAlias(String alais) {
        return registry.getAddressForAlias(alais);
    }

    public void registerAlias(String alias, Address address)
    {
        registry.registerAlias(alias, address);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Collector getCollector() {
        return collector;
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }

    public Router getParent() {
        return parent;
    }

    public void setParent(Router parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Router router = (Router) o;

        if (collector != null ? !collector.equals(router.collector) : router.collector != null) return false;
        if (dispatcher != null ? !dispatcher.equals(router.dispatcher) : router.dispatcher != null) return false;
        if (parent != null ? !parent.equals(router.parent) : router.parent != null) return false;
        if (registry != null ? !registry.equals(router.registry) : router.registry != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (registry != null ? registry.hashCode() : 0);
        result = 31 * result + (dispatcher != null ? dispatcher.hashCode() : 0);
        result = 31 * result + (collector != null ? collector.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
