package com.eis.b2bmb.routerPOC.model;

import com.eis.core.api.v1.model.Mailbox;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 2:50 PM
 */
// FIXME: should derive from BaseObject but for now simulating it with refName and datadomain local vars.
public class Node  {

    protected String refName;
    protected String dataDomain;
    protected Address address;
    protected Mailbox inbox;
    protected Mailbox outBox;


    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getDataDomain() {
        return dataDomain;
    }

    public void setDataDomain(String dataDomain) {
        this.dataDomain = dataDomain;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Mailbox getInbox() {
        return inbox;
    }

    public void setInbox(Mailbox inbox) {
        this.inbox = inbox;
    }

    public Mailbox getOutBox() {
        return outBox;
    }

    public void setOutBox(Mailbox outBox) {
        this.outBox = outBox;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (address != null ? !address.equals(node.address) : node.address != null) return false;
        if (inbox != null ? !inbox.equals(node.inbox) : node.inbox != null) return false;
        if (refName != null ? !refName.equals(node.refName) : node.refName != null) return false;
        if (outBox != null ? !outBox.equals(node.outBox) : node.outBox != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = refName != null ? refName.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (inbox != null ? inbox.hashCode() : 0);
        result = 31 * result + (outBox != null ? outBox.hashCode() : 0);
        return result;
    }
}
