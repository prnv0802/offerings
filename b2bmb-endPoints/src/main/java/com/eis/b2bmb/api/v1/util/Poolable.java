package com.eis.b2bmb.api.v1.util;
/**
 * This interface is used to make the object poolable.
 * @author sudhakars
 * date - 19/05/14
 */
public interface Poolable {

    /**
     * performs clean up operations on the object before returning it to the
     * pool
     */
    public void clear();
}