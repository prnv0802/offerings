package com.eis.b2bmb.endpts.ftp;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 4:46 PM
 */
public class FTPUser implements User {

    /**
     * The id of the user
     */
    protected String name;

    /**
     * Users password
     */
    protected String password;

    /**
     * how long to allow them to be idle before logging them off automatically
     */
    protected int maxIdleTimeSec;

    /**
     * If the user is enabled or not
     */
    protected boolean enabled;

    /**
     * The user's home directory
     */
    protected String homeDirectory;


    /**
     * List of authorities
     */
    private List<Authority> authorities = new ArrayList<Authority>();



    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public List<Authority> getAuthorities() {
        if (authorities != null) {
            return Collections.unmodifiableList(authorities);
        } else {
            return null;
        }
    }

    @Override
    public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
        List<Authority> selected = new ArrayList<Authority>();

        for (Authority authority : authorities) {
            if (authority.getClass().equals(clazz)) {
                selected.add(authority);
            }
        }

        return selected;
    }

    /**
     * Sets the list of authorities to check
     * @param authorities the list of authorities
     */
    public void setAuthorities(List<Authority> authorities) {
        if (authorities != null) {
            this.authorities = Collections.unmodifiableList(authorities);
        } else {
            this.authorities = null;
        }
    }

    @Override
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        // check for no authorities at all
        if(authorities == null) {
            return null;
        }

        boolean someoneCouldAuthorize = false;
        for (Authority authority : authorities) {
            if (authority.canAuthorize(request)) {
                someoneCouldAuthorize = true;

                request = authority.authorize(request);

                // authorization failed, return null
                if (request == null) {
                    return null;
                }
            }

        }

        if (someoneCouldAuthorize) {
            return request;
        } else {
            return null;
        }
    }

    @Override
    public int getMaxIdleTime() {
        return maxIdleTimeSec;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public String getHomeDirectory() {
        return homeDirectory;
    }

    /**
     * sets the user's name
     * @param name their name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * sets their password
     * @param password their password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the maxium time the user can be idle
     * @param maxIdleTime - the numberof seconds
     */
    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTimeSec = maxIdleTime;
        if (maxIdleTimeSec < 0) {
            maxIdleTimeSec = 0;
        }
    }

    /**
     * if this user is enabled or not
     * @return true its enabled / false its not
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets weather this user is enabled or not
     * @param enabled - the value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the home directory
     * @param homeDirectory the absolute path of the directory
     */
    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }
}
