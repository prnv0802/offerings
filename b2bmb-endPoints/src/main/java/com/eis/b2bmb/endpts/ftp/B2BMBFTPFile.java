package com.eis.b2bmb.endpts.ftp;

import com.eis.core.api.v1.model.FileSystemEntry;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 10:36 AM
 */
public class B2BMBFTPFile implements FtpFile {


    /**
     * The file Entry this file points to
     */
    protected FileSystemEntry entry;

    /**
     * The user
     */
    protected User user;

    /**
     * The file name
     */
    protected String fileName;

    /**
     * Creates a new File
     * @param fileName the name of the file
     * @param entry the entry that maps to the b2bmb file system
     * @param user the user
     */
    public B2BMBFTPFile(String fileName, FileSystemEntry entry, User user)
    {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName can not be null");
        }
        if (entry == null) {
            throw new IllegalArgumentException("entry can not be null");
        }

        if (fileName.length() == 0) {
            throw new IllegalArgumentException("fileName can not be empty");
        } else if (fileName.charAt(0) != '/') {
            throw new IllegalArgumentException(
                    "fileName must be an absolute path");
        }

        this.fileName = fileName;
        this.entry = entry;
        this.user = user;

    }


    @Override
    public String getAbsolutePath() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean doesExist() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public int getLinkCount() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public boolean setLastModified(long l) {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean move(FtpFile ftpFile) {
        return false;
    }

    @Override
    public List<FtpFile> listFiles() {
        return null;
    }

    @Override
    public OutputStream createOutputStream(long l) throws IOException {
        return null;
    }

    @Override
    public InputStream createInputStream(long l) throws IOException {
        return null;
    }
}
