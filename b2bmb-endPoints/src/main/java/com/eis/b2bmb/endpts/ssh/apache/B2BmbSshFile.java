package com.eis.b2bmb.endpts.ssh.apache;

import com.eis.core.api.v1.service.BlobService;
import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.Blob;
import org.apache.sshd.common.file.SshFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: mingardia
 * Date: 10/16/13
 * Time: 2:50 PM
 */
public class B2BmbSshFile implements SshFile {

    private static final Logger LOG = LoggerFactory.getLogger(B2BmbSshFile.class);

    /**
     * The BlobService implementation to use
     */
    protected BlobService blobService;

    /**
     * The blob that represents this file / directory object
     */
    Blob blob;


    /**
     * Constructor
     *
     * @param service  the blobStore Service we want to use
     * @param fileName the fileName of the file we want this SShFile to represent. File is assumed to be in the root
     *                 directory
     * @throws B2BNotAuthorizedException    - if the currently logged in user is not authorized to view the file
     * @throws B2BTransactionFailed         - some thing went wrong trying to retrieve the file
     * @throws B2BNotAuthenticatedException - there is not authenticated user.
     */
    public B2BmbSshFile(BlobService service, String fileName) throws B2BNotAuthorizedException, B2BTransactionFailed,
            B2BNotAuthenticatedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating instance of a b2bmbsshFile with file name:" + fileName);
        }

        if (service == null) {
            throw new IllegalArgumentException("BlobService can not be null");
        }


        this.blobService = service;


        blob = blobService.getByRefName(fileName);
    }

    /**
     * Constructor
     *
     * @param service  the blobStore service we want to use
     * @param baseDir  the directory where to look for the file
     * @param fileName the file name
     * @throws B2BNotAuthorizedException    - if the user is not authorized.
     * @throws B2BTransactionFailed         - if the transaction failed / or some other datasource issue occured
     * @throws B2BNotAuthenticatedException - if the calling user is not authenticated.
     */
    public B2BmbSshFile(BlobService service, SshFile baseDir, String fileName) throws B2BNotAuthorizedException,
            B2BTransactionFailed, B2BNotAuthenticatedException {
        this.blobService = service;
        blob = blobService.getByRefName(baseDir + "/" + fileName);

        // if (blob == null)
        // {
        //      throw new IllegalStateException("Blob should not be null");
        //  }

    }

    @Override
    public String getAbsolutePath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving absolute path");
        }

        return ".";
        // return blob.getMetadata().getPathString();  //To change body of implemented methods use File | Settings |
        // File Templates.
    }

    @Override
    public String getName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning the fileName");
        }
        return ".";
        //return blob.getName();
    }

    @Override
    public Map<Attribute, Object> getAttributes(boolean b) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributes:" + b);
        }

        return new HashMap<Attribute, Object>();
    }

    @Override
    public void setAttributes(Map<Attribute, Object> attributeObjectMap) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAttributes:");
        }
    }

    @Override
    public Object getAttribute(Attribute attribute, boolean b) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttribute:" + attribute.toString() + " b:" + b);
        }

        return null;
    }

    @Override
    public void setAttribute(Attribute attribute, Object o) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAttribute:" + attribute.toString(), " Object o:" + o.toString());
        }
    }

    @Override
    public String readSymbolicLink() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("readSymbolicLink");
        }

        return null;
    }

    @Override
    public String getOwner() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOwner");
        }

        return null;
    }

    @Override
    public boolean isDirectory() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDirectory");
        }
        return false;
    }

    @Override
    public boolean isFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isFile");
        }
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean doesExist() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doesExist");
        }

        return true;
    }

    @Override
    public boolean isReadable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isReadable");
        }
        return true;
    }

    @Override
    public boolean isWritable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isWriteable");
        }
        return false;
    }

    @Override
    public boolean isExecutable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isExecutable");
        }
        return false;
    }

    @Override
    public boolean isRemovable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRemovable");
        }
        return false;
    }

    @Override
    public SshFile getParentFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParentFile");
        }
        return null;
    }

    @Override
    public long getLastModified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLastModified");
        }
        return -1;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setLastModified(long l) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLastModified" + l);
        }
        return false;
    }

    @Override
    public long getSize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSize");
        }
        return blob.getSize();
    }

    @Override
    public boolean mkdir() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mkdir");
        }
        return false;
    }

    @Override
    public boolean delete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete");
        }
        return false;
    }

    @Override
    public boolean create() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }

        return false;
    }

    @Override
    public void truncate() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("truncate");
        }
    }

    @Override
    public boolean move(SshFile sshFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("move");
        }
        return false;
    }

    @Override
    public List<SshFile> listSshFiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listSshFiles");
        }
        return new ArrayList<SshFile>();
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createOutputStream:" + offset);
        }

        return null;
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createInputStream" + offset);
        }

        return null;
    }

    @Override
    public void handleClose() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handleClose");
        }
    }
}
