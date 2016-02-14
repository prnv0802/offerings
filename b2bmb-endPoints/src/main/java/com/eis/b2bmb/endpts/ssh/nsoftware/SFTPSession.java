package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.core.api.v1.model.Transmission;
import org.apache.shiro.subject.Subject;

import com.eis.b2bmb.api.v1.model.SFTPServerUser;

/**
 * Represents a SFTPSession
 */
public class SFTPSession
{
    /**
     * the connection id
     */
    protected String connectionId;

    /**
     * The userid assocaited to the server
     */
    protected String user;
    
    /**
     * Sftp User current logged in for current session 
     */
    protected SFTPServerUser sftpServerUser;

    /**
     * The subject that is associated with this connection
     */
    protected Subject subject;

    /**
     *  Transmission being recorded for this session.
     */
    protected Transmission transmission;

    /**
     * Gets the connection id
     * @return the connection id
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the connection id
     * @param connectionId the connection id
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * gets the userId for this session
     * @return the user id
     */
    public String getUser() {
        return user;
    }


    /**
     * sets the userId for this session
     * @param user the user Id
     */
    public void setUser(String user) {
        this.user = user;
    }


    /**
     * Gets the Subject
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Sets the subject
     * @param subject the subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

     /**
      * @return sftpUser
      */
     public SFTPServerUser getSftpServerUser() {
          return sftpServerUser;
     }

     /**
      * @param sftpServerUser  sftpUser
      */
     public void setSftpServerUser(SFTPServerUser sftpServerUser) {
          this.sftpServerUser = sftpServerUser;
     }

    /**
     * Returns the Transmission being recorded for this session.
     *
     * @return - Transmission object
     */
    public Transmission getTransmission() {
        return transmission;
    }

    /**
     * Sets the Transmission being recorded for this session.
     *
     * @param transmission - Transmission
     */
    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }
}
