package com.eis.core.api.v1.model;

/**
 * User: mingardia
 * Date: 9/24/13
 * Time: 12:33 PM
 */
public enum EndPointType {
    HTTP_REST,
    AS2,
    SFTP;


    public String value() {
        return name();
    }

    public static EndPointType fromValue(String v) {
        return valueOf(v);
    }
}
