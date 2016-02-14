package com.eis.b2bmb.endpts.ssh.nsoftware;

/**
 * Lock operation requested for SSH file open.
 *
 * @author Praveen S Rao
 */
public enum LockOperation {

    READ, WRITE, APPEND, CREATE, TRUNCATE, EXCLUSIVE, NA

}
