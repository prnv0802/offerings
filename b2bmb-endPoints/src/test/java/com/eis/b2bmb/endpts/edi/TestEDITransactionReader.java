package com.eis.b2bmb.endpts.edi;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.b2bmb.util.EDITransactionReader;
import com.eis.base.test.TestBase;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.BlobStore;
import com.eis.core.api.v1.model.Mailbox;
import com.eis.core.api.v1.model.MailboxEntry;
import com.eis.core.api.v1.model.MailboxRole;
import inedi.InEDIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * User: mingardia
 * Date: 10/26/13
 * Time: 8:15 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
public class TestEDITransactionReader extends TestBase{

    String interchangeHeader = null;
    String groupHeader = null;

    /**
     * blobDAO - injected by camel
     */
    @Autowired
    protected BlobDAO blobDAO;

    /**
     * blobStore - injected by camel
     */
    @Autowired
    protected BlobStore blobStore;

    /**
     * mailboxEntryDAO - injected by camel
     */
    @Autowired
    MailboxEntryDAO mailboxEntryDAO;

    /**
     * mailboxDAO - injected by camel
     */
    @Autowired
    MailboxDAO mailboxDAO;

    /**
     * availableDocumentDAO - injected by camel
     */
    @Autowired
    AvailableDocumentDAO availableDocumentDAO;

    @Test
    public void testEDIReader() throws InEDIException, TooManyListenersException, B2BNotFoundException,
            B2BTransactionFailed, B2BNotAuthenticatedException, B2BNotAuthorizedException,
            FileNotFoundException, B2BNotFoundException, ValidationException {
        Mailbox mailbox = null;
        List<String> mailboxEntryIds = new ArrayList<String>();
        List<MailboxEntry> mbes = null;
        serverSideLogin();

        try
        {
            mailbox = new Mailbox();
            mailbox.setId(String.valueOf(UUID.randomUUID()));
            mailbox.setRefName("TestEDIMailbox");
            mailbox.setDataDomain("com.mycompanyxyz");
            mailbox.setMailboxRole(MailboxRole.INTERMEDIATE);
            mailboxDAO.save(mailbox);

            EDITransactionReader reader = new EDITransactionReader();
            Iterator iterator = reader.getTransactions(
                     getClass().getClassLoader().getResourceAsStream("sampleEDIMultipleTransactions.edi"),
                     blobStore, mailboxEntryDAO, availableDocumentDAO,
                     mailbox, String.valueOf(UUID.randomUUID()),
                    "test1@mycompanyxyz.com", "test2@mycompanyxyz.com");
            int count = 0;
            while(iterator.hasNext()) {
                String transaction = (String) iterator.next();
                count++;
            }
            assertEquals(5, count);
            mbes = mailboxEntryDAO.getByMailboxRefName("TestEDIMailbox",0,10,null,
                    "com.mycompanyxyz");
            assertEquals(5, mbes.size());


        }
        finally {

            if(mbes != null) {
                for (MailboxEntry mbe : mbes) {
                    mailboxEntryDAO.delete(mbe);
                }
            }

            if(mailbox != null) {
                mailboxDAO.delete(mailbox);
            }

            logout();
        }
    }
}
