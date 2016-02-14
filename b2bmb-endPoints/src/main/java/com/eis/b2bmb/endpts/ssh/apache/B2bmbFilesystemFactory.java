package com.eis.b2bmb.endpts.ssh.apache;

import com.eis.core.api.v1.service.BlobService;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.FileSystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * User: mingardia
 * Date: 10/16/13
 * Time: 2:27 PM
 */
public class B2bmbFilesystemFactory implements FileSystemFactory {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFilesystemFactory.class);

    @Autowired
    BlobService blobService;


    /**
     * Blank Constructor
     */
    public B2bmbFilesystemFactory()
    {
        super();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("**** B2bmbFilesystemFactory Created..");
        }

    }

    @Override
    public FileSystemView createFileSystemView(Session session) throws IOException {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("**** Creating File System View..");
        }

        if (blobService == null)
        {
            throw new IllegalStateException("blobService was not injected check spring config");
        }

        return new B2bmbFileSystemView(blobService);
    }
}
