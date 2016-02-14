package com.eis.b2bmb.routerPOC.service;

import com.eis.b2bmb.routerPOC.model.Address;
import com.eis.b2bmb.routerPOC.model.Dispatcher;
import com.eis.b2bmb.routerPOC.model.Node;
import com.eis.b2bmb.routerPOC.model.Router;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.Mailbox;
import com.eis.core.api.v1.model.MailboxEntry;
import com.eis.security.multitenancy.model.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 3:07 PM
 */
public class HiearchicalDispatcherImpl implements Dispatcher {

    protected Router router;
    protected MailboxEntryDAO mailboxEntryDAO;
    private static final Logger LOG = LoggerFactory.getLogger(HiearchicalDispatcherImpl.class);


    public HiearchicalDispatcherImpl(Router router)
    {
        this.router = router;
    }

    // Real simple algorithm
    // grab everything in the inbox
    // try to find it in the registry
    // if its in the registry put the message in that node's inbox.
    // if we can't find any recipients put the message in our outbox.
    @Override
    public void dispatch() throws B2BTransactionFailed, B2BNotFoundException, ValidationException {
        Mailbox inbox = router.getInbox();
        long entryCount = mailboxEntryDAO.getCountByMailboxRefName("INBOX", router.getDataDomain());

        List<String> dataDomains = new ArrayList<>();
        dataDomains.add(router.getDataDomain());

        int count=0;

        if (entryCount < Integer.MAX_VALUE)
        {
            count = (int) entryCount;
        }
        else
        {
            count = Integer.MAX_VALUE;
        }

        List<MailboxEntry> entries = mailboxEntryDAO.getList(0, count, null, dataDomains);

        for (MailboxEntry entry : entries)
        {
            String toAlias = entry.getToUserId();
            Address address = router.getAddressForAlias(toAlias);

            if (address != null)
            {

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found address in router's registry");
                }

                Node destinationNode =   router.getNode(address);

                if (destinationNode != null)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Found node for address:" + address.toString()  + " Node:" + destinationNode.getRefName());
                    }

                    Mailbox nodeInbox = destinationNode.getInbox();

                    entry.setId(null);
                    entry.setMailboxId(nodeInbox.getId());
                    entry.setDataDomain(nodeInbox.getDataDomain());
                    entry.setTxId(SecureSession.getTxId());

                    mailboxEntryDAO.save(entry);

                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Could not find node for address:" + address.toString() + " placing in router's outbox");
                    }

                    Mailbox outBox = router.getOutBox();

                    entry.setId(null);
                    entry.setMailboxId(outBox.getId());
                    entry.setDataDomain(outBox.getDataDomain());
                    entry.setTxId(SecureSession.getTxId());

                    mailboxEntryDAO.save(entry);


                }
            }
            else
            {
                // Until we introduce a dead letter queue and a better way for the mailentry to have both a from and to based upon a user
                // I will just put anything we can't find in the outbox.
                //if (router.getParent() != null)
                //{
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Could not find the address:" + address.toString() + " for the alias:" + toAlias + " putting in router's outbox");
                    }

                    Mailbox outBox = router.getOutBox();

                    entry.setId(null);
                    entry.setMailboxId(outBox.getId());
                    entry.setDataDomain(outBox.getDataDomain());
                    entry.setTxId(SecureSession.getTxId());

                    mailboxEntryDAO.save(entry);
                //}

            }

        }

    }
}
