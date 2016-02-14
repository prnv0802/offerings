package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.license.LicenseConstants;
import ipworksssh.Certificate;
import ipworksssh.IPWorksSSHException;
import ipworksssh.Sftpserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.TooManyListenersException;




/**
 * User: mingardia Date: 11/5/13 Time: 6:14 PM
 */
public class SFTPServer extends Sftpserver implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(SFTPServer.class);

    /** unset **/
    public static final int UNSET_STATUS=-9999;

    /** good return code **/
    public static final int SSH_FX_OK =0;

    /** end of file **/
    public static final int SSH_FX_EOF=1;


    /** no such file exists **/
    public static final int SSH_FX_NO_SUCH_FILE= 2;

    /** permission denied **/
    public static final int SSH_FX_PERMISSION_DENIED= 3 ;

    /** something failed check the log **/
    public static final int SSH_FX_FAILURE= 4;

    /** bad message **/
    public static final int SSH_FX_BAD_MESSAGE =5;

    /** no connection exists **/
    public static final int SSH_FX_NO_CONNECTION= 6;

    /** connection was lost **/
    public static final int SSH_FX_CONNECTION_LOST= 7;

    /** Operation is not supported by this implementation **/
    public static final int SSH_FX_OP_UNSUPPORTED= 8;

    /** The handle is not valid **/
    public static final int SSH_FX_INVALID_HANDLE= 9;

    /** Path does not exist **/
    public static final int SSH_FX_NO_SUCH_PATH =10;

    /** file already exists **/
    public static final int SSH_FX_FILE_ALREADY_EXISTS= 11;

    /** the file is write protected **/
    public static final int SSH_FX_WRITE_PROTECT= 12;

    /** the disk we are using has been disconnected */
    public static final int SSH_FX_NO_MEDIA= 13;

    /** out of disk space */
    public static final int SSH_FX_NO_SPACE_ON_FILESYSTEM= 14;

    /** there is disk space but the user hit their quota */
    public static final int SSH_FX_QUOTA_EXCEEDED= 15;

    /** unknown user **/
    public static final int SSH_FX_UNKNOWN_PRINCIPAL= 16;

    /** Locking conflict **/
    public static final int SSH_FX_LOCK_CONFLICT= 17;

    /** Directory is not empty **/
    public static final int SSH_FX_DIR_NOT_EMPTY= 18;

    /** not a directory **/
    public static final int SSH_FX_NOT_A_DIRECTORY= 19;

    /** invalid file name **/
    public static final int SSH_FX_INVALID_FILENAME =20;

    /** Link loop detected **/
    public static final int SSH_FX_LINK_LOOP =21;

    /** delete failed **/
    public static final int SSH_FX_CANNOT_DELETE= 22;

    /** invalid argument **/
    public static final int SSH_FX_INVALID_PARAMETER= 23;

    /** file is a directory **/
    public static final int SSH_FX_FILE_IS_A_DIRECTORY= 24;

    /** could not acquire lock **/
    public static final int SSH_FX_BYTE_RANGE_LOCK_CONFLICT= 25;

    /** could not acquire lock **/
    public static final int SSH_FX_BYTE_RANGE_LOCK_REFUSED= 26;

    /** delete pending **/
    public static final int SSH_FX_DELETE_PENDING= 27;

    /** file is corrupt **/
    public static final int SSH_FX_FILE_CORRUPT= 28;

    /** owner invalid **/
    public static final int SSH_FX_OWNER_INVALID= 29;

    /** group invalid **/
    public static final int SSH_FX_GROUP_INVALID= 30;

    /** byte range does not match **/
    public static final int SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK= 31;


    // File Permissions

    /** Read Access **/
    public static final int SSH_FXF_READ = 1;

    /** Write Access **/
    public static final int SSH_FXF_WRITE = 2;

    /** Append Access **/
    public static final int SSH_FXF_APPEND = 4;

    /** Create Access **/
    public static final int SSH_FXF_CREAT = 8;

    /** Truncate **/
    public static final int SSH_FXF_TRUNC = 10;

    /** Exclusive Access **/
    public static final int SSH_FXF_EXCL = 20;

    /**
     * Default constructor
     */
    public SFTPServer() {

        setRuntimeLicense(LicenseConstants.getSSHLicenseString());

        if (LOG.isDebugEnabled()) {
            LOG.debug("SFTP Instance Created ...");
        }
      

        try {
            config("CloseStreamAfterTransfer=false");
            config("InBufferSize=10000");
            config("OutBufferSize=10000");
            config("TcpNoDelay=true");
            setSSHCert(new Certificate(Certificate.cstPFXFile, "sftpserver.pfx", "demo",
                    "*"));




        } catch (IPWorksSSHException e) {
            e.printStackTrace();
        }

    }

    /**
     * sets the listener
     * @param listener the listener
     */
    public void setListener(SFTPServerListener listener) {

        try {
            addSftpserverEventListener(listener);
        } catch (TooManyListenersException e) {
            throw new IllegalArgumentException(
                    "Too many listeners should never be thrown here", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (LOG.isInfoEnabled())
        {
            LOG.info("**** Shutting down SFTP Server ");

        }
        try {
            if (getConnections().keySet().isEmpty())
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("> Idle server shutting down");
                }

                shutdown();
            }
            else
            {
                setListening(false);

                int count=0;
                while (!getConnections().isEmpty() && count < 30)
                {
                    if (LOG.isInfoEnabled())
                    {
                        LOG.info("> Connected clients shutting down listener and waiting for connections to close");
                        LOG.info("> Number of connected clients:" + getConnections().keySet().size());
                        LOG.info("> Count:" + count);
                    }

                    Thread.currentThread().sleep(1000);
                    count++;
                }

                shutdown();

            }

        } catch (IPWorksSSHException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
