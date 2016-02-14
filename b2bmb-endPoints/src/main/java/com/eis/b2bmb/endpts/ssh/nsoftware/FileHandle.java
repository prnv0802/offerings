package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.core.api.v1.model.BlobHandle;

/**
 * User: mingardia
 * Date: 3/13/14
 * Time: 2:28 PM
 */
public class FileHandle {
    /**
     * The blob handle we are using
     */
    protected BlobHandle handle;

    /**
     * Constructor
     * @param handle the handle we want to wrap
     */
    public FileHandle(BlobHandle handle)
    {
        this.handle = handle;
    }

    /**
     * retreives the handle we constructed with
     * @return the handle
     */
    public BlobHandle getBlobHandle()
    {
        return handle;
    }
}
