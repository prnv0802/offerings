package com.eis.b2bmb.routerPOC.model;

import java.util.LinkedHashMap;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 2:52 PM
 */
public class Registry {
    protected LinkedHashMap<Address, Node> nodeMap = new LinkedHashMap<>();

    protected LinkedHashMap<String, Address> aliasMap = new LinkedHashMap<>();


    public LinkedHashMap<Address, Node> getNodeMap() {
        return nodeMap;
    }

    public LinkedHashMap<String, Address> getAliasMap() { return aliasMap;}

    public Address getAddressForAlias(String alias)
    {
        return aliasMap.get(alias);
    }

    public void registerAlias(String alias, Address address)
    {
        aliasMap.put(alias, address);
    }


}
