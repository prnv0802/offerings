package com.eis.b2bmb.endpts.ssh.apache;

import com.eis.core.api.v1.service.BlobService;
import com.eis.core.api.v1.exception.B2BException;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.SshFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mingardia
 * Date: 10/16/13
 * Time: 2:50 PM
 */
public class B2bmbFileSystemView implements FileSystemView {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFileSystemView.class);

    /**
     * The Blob service implementation to use
     */
    protected BlobService blobService;


    /**
     * constructor
     * @param blobService - the blobService implementation to use
     */
    public B2bmbFileSystemView(BlobService blobService)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("B2bmbFileSystemView created");
        }

        if (blobService == null)
        {
            throw new IllegalArgumentException("Blob service can not be null");
        }

        this.blobService = blobService;
    }

    @Override
    public SshFile getFile(String fileName) {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("getfile:" + fileName);
        }

        SshFile file= null;
        try {
            file =  new B2BmbSshFile(blobService, fileName);
        } catch (B2BException e) {
            // ignore for now
        }

        return file;
    }

    @Override
    public SshFile getFile(SshFile baseDir, String fileName) {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("getFile baseDir:" + baseDir + " fileName:" + fileName);
        }

        SshFile file = null;

        try
        {
            file = new B2BmbSshFile(blobService, baseDir, fileName);
        }
        catch (B2BException e)
        {
            // ignore for now
        }

        return file;


    }
}
