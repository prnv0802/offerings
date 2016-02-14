package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.Mailbox;
import com.eis.core.api.v1.model.MailboxEntry;
import com.eis.core.api.v1.model.MailboxRole;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class TranslateQueueProcessor implements Processor  {

        @Autowired
        EDIProfileDAO ediProfileDAO;

        @Autowired
        MailboxEntryDAO mailboxEntryDAO;

        @Autowired
        MailboxDAO mailboxDAO;

        @Override
        public void process(Exchange exchange) throws Exception {

            String mailId = exchange.getIn().getBody(String.class);
            String groupId = (String) exchange.getIn().getHeaders().get("JMSXGroupID");


            System.out.println("Message:" + groupId + ":" + mailId);
            if (groupId != null) {
                String[] profileInfo = groupId.split(":");
                if (profileInfo.length == 3) {
                    String fromAddress = profileInfo[0];
                    String toAddress = profileInfo[1];
                    String docType = profileInfo[2];
                    try {
                        EDIProfile ediProfile = ediProfileDAO.getEDIProfile(fromAddress, toAddress, docType);

                        if (ediProfile != null) {
                            BigInteger interchangeControlNumber = ediProfile.getLastInterchangeControlNumber();

                            MailboxEntry mailboxEntry =  mailboxEntryDAO.getById(mailId);
                            if(mailboxEntry != null) {
                                BigInteger mailSequenceId = mailboxEntry.getSequenceNumber();

                                if(interchangeControlNumber.equals(mailSequenceId)) {

                                    interchangeControlNumber.add(BigInteger.ONE);
                                    ediProfile.setLastInterchangeControlNumber(interchangeControlNumber);
                                    ediProfileDAO.save(ediProfile);
                                    exchange.getIn().setBody(mailId);
                                } else {

                                        String mailboxName = exchange.getIn().getHeader("MailboxName", String.class);
                                        String domain = exchange.getIn().getHeader("Domain", String.class);
                                        Mailbox mailbox = mailboxDAO.getByRefName(mailboxName,
                                                domain);
                                        if (mailbox == null) {
                                            Mailbox processedMailbox = new Mailbox();
                                            processedMailbox.setDataDomain(domain);
                                            processedMailbox.setRefName(mailboxName);
                                            processedMailbox.setMailboxRole(MailboxRole.INTERMEDIATE);
                                            processedMailbox.setSystemMailbox(true);
                                            mailbox = mailboxDAO.save(processedMailbox);
                                        }


                                        mailboxEntryDAO.moveEntryAndChangeState(mailbox.getId(), mailboxEntry.getId(),
                                                 mailboxEntry.getState());
                                }

                            }
                        }
                    } catch (B2BNotFoundException e) {

                    } catch (B2BTransactionFailed e) {

                    }
                }
            }
        }


}
