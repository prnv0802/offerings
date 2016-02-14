package com.eis.b2bmb.routerPOC.test;

import com.eis.b2bmb.routerPOC.model.Address;
import com.eis.b2bmb.routerPOC.model.Node;
import com.eis.b2bmb.routerPOC.model.Router;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.Mailbox;
import com.eis.core.api.v1.model.MailboxEntry;
import org.junit.Test;

import java.util.UUID;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 10:22 PM
 */

public class TestMailRouter {
    /**


         Define the following structure:

                               Router
                   /                            \
                  |                              \
                  hhgreg.com                    samsung.com
          /      /                             \                \
         |    rmoore@hhgreg.com        stung@samsung.com  orders@samsung.com
 invoices@hhgregg.com



     */
    @Test
    public void testBasicsOfRouter() throws ValidationException {
        Router router = new Router();
        router.setRefName("MyRouter");
        router.setDataDomain("app.cantata.myrouter");

        Address.AddressBuilder routerAddressBuilder = new Address.AddressBuilder();
        routerAddressBuilder.setEnv("DEV");
        routerAddressBuilder.setOrgRefName("*");
        routerAddressBuilder.setRegion("01");
        routerAddressBuilder.setRouter("01");
        routerAddressBuilder.setCountry("US");
        routerAddressBuilder.setNodeRefName("router01");
        router.setAddress(routerAddressBuilder.build());

        Node hhgregg = new Node();
        hhgregg.setRefName("hhgregg");

        Address.AddressBuilder addressBuilder = new Address.AddressBuilder();
        addressBuilder.setEnv(routerAddressBuilder.getEnv());
        addressBuilder.setCountry(routerAddressBuilder.getCountry());
        addressBuilder.setRouter(routerAddressBuilder.getRouter());
        addressBuilder.setRegion(routerAddressBuilder.getRegion());
        addressBuilder.setOrgRefName("hhgregg");
        addressBuilder.setNodeRefName("hhgreg.com");

        hhgregg.setAddress(addressBuilder.build());

        Mailbox mbox = new Mailbox();
        mbox.setId(String.valueOf(UUID.randomUUID()));
        mbox.setRefName("INBOX");
        mbox.setDataDomain("com.hhgregg");
        hhgregg.setInbox(mbox);

        mbox = new Mailbox();
        mbox.setId(String.valueOf(UUID.randomUUID()));
        mbox.setRefName("OUTBOX");
        mbox.setDataDomain("com.hhgregg");
        hhgregg.setOutBox(mbox);

        router.register(hhgregg);
        router.registerAlias("rmoore@hhgregg.com", hhgregg.getAddress());


        Node samsung = new Node();
        samsung.setRefName("samsung");


        addressBuilder.setOrgRefName("samsung");
        addressBuilder.setNodeRefName("samsung.com");
        samsung.setAddress(addressBuilder.build());

        mbox.setId(String.valueOf(UUID.randomUUID()));
        mbox.setRefName("INBOX");
        mbox.setDataDomain("com.samsung");
        hhgregg.setInbox(mbox);

        mbox = new Mailbox();
        mbox.setId(String.valueOf(UUID.randomUUID()));
        mbox.setRefName("OUTBOX");
        mbox.setDataDomain("com.samsung");
        hhgregg.setOutBox(mbox);


        router.register(samsung);
        router.registerAlias("stung@samsung.com", samsung.getAddress());

        MailboxEntry entry = new MailboxEntry();

        // FIXME: NEED to fix mailbox to be from user to user.
        //entry.setFromAccountRefId();



    }

}
