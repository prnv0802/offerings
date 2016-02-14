package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.b2bmb.api.v1.util.Poolable;
/**
 * This class is being used with SFTPServerHelper.
 * @author sudhakars
 */
public class OpenFileKey implements Poolable{
    /**
     * connectionId
     */
    String connectionId;
    /**
     * Path of the server.
     */
    String path;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenFileKey key = (OpenFileKey) o;

        if (connectionId != null ? !connectionId.equals(key.connectionId) : key.connectionId != null) return false;
        if (path != null ? !path.equals(key.path) : key.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = connectionId != null ? connectionId.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public void clear() {
        this.connectionId = null;
        this.path = null;
    }
}
