package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.exception.MapForceServerException;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.camel.custom.util.MailboxEntryHelper;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.BlobStoreException;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.Attachment;
import com.eis.core.api.v1.model.BlobStore;
import com.eis.core.api.v1.model.Mailbox;
import com.eis.core.api.v1.model.MailboxEntry;
import org.apache.camel.Exchange;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.Processor;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.IOHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Processor that is used to call Map Force Server when not using EDI Profile configurations.  This
 * Processor requires the following Camel Exchange Headers be passed in:
 *
 * to: B2BMailbox internal to address
 * from: B2BMailbox internal from address
 * MapServiceName: name of deployed map on MapForce Server, ex: service/canonical/4010/850editoxml
 * MapForceServerUrl: url of map force server, ex: http://flowforceserver.enspirecommerce.com
 *
 */
public class MapForceMapServerProcessor implements Processor  {

    private static final Logger LOG = LoggerFactory.getLogger(MapForceMapServerProcessor.class);

    /**
     * mailbox entry dao
     */
    @Autowired
    protected MailboxEntryDAO mailboxEntryDAO;

    /**
     * mailbox dao
     */
    @Autowired
    protected MailboxDAO mailboxDAO;

    /**
     * blob dao
     */
    @Autowired
    protected BlobDAO blobDAO;

    /**
     * blobStore - injected by camel
     */
    @Autowired
    BlobStore blobStore;

    @Override
    public void process(Exchange exchange) throws Exception {

        InputStream file = exchange.getIn().getBody(InputStream.class);
        String to = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String from = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        String subject = exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class);
        InputStream in = null;

        try {
            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
            String mapServiceName = ExchangeHelper.getMandatoryHeader(exchange,
                    B2bmbCamelConstants.MAP_FORCE_SERVICE_NAME, String.class);
            String mapForceServerUrl = ExchangeHelper.getMandatoryHeader(exchange,
                    B2bmbCamelConstants.MAP_FORCE_SERVER_URL, String.class);
            String url = mapForceServerUrl + mapServiceName;

            if(LOG.isInfoEnabled()) {
                LOG.info("Attempting to call MapForceServer url:"+url+" for to:"+to+ ", from:"+from);
            }

            Mailbox mailbox = MailboxEntryHelper.getMailbox(mailboxDAO, exchange, "map-processing");
            if(mailbox == null) {
                throw new IllegalStateException("Mailbox with name: map-processing could not be found or " +
                        "created.");
            }

            MailboxEntry mailboxEntry = MailboxEntryHelper.createMailboxEntry(exchange, mailbox,
                    blobStore, mailboxEntryDAO,
                    to, from, subject, file, UUID.randomUUID() + "-processing");

            if(mailboxEntry == null) {
                throw new IllegalStateException("MailboxEntry with name: map-processing could not be found or " +
                        "created.");
            }

            Attachment attachment = null;
            if(mailboxEntry.getAttachments() == null || mailboxEntry.getAttachments().size() == 0) {
                throw new IllegalStateException("There were no attachments for mailbox entry id:"+
                        mailboxEntry.getId());
            }

            // we just created the mailbox entry, should only be one attachment
            attachment = mailboxEntry.getAttachments().get(0);
            file = MailboxEntryHelper.getInputStreamFromAttachment(
                    attachment, blobDAO);

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", file, ContentType.DEFAULT_TEXT, fileName);

            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);

            if(response.getStatusLine().getStatusCode() == 200) {

                if(LOG.isInfoEnabled()) {
                    LOG.info("Mapping request to MapForce Server url:"+url+" was successful. Response was:"+
                            response.getStatusLine().getStatusCode());
                }
                InputStream inputStream = doExtractResponseBodyAsStream(response.getEntity().getContent(),
                        exchange);

                exchange.getIn().setBody(inputStream);
            } else {
                if(LOG.isErrorEnabled()) {
                    LOG.error("There was a problem generating map from MapForceServer at:" +
                            url + "The status code was:" + response.getStatusLine().getStatusCode() + ", reason:"+
                            response.getStatusLine().getReasonPhrase());
                }
                attachment = mailboxEntry.getAttachments().get(0);
                file = MailboxEntryHelper.getInputStreamFromAttachment(
                        attachment, blobDAO);
                createMapForceServerErrorMailboxEntry(to, from, subject, file, exchange);
                throw new MapForceServerException("There was a problem generating map from MapForceServer at:"+
                        url+"The status code was:"+response.getStatusLine().getStatusCode()+ ", reason:"+
                        response.getStatusLine().getReasonPhrase());

            }

        } finally {
           if(file != null) {
               file.close();
           }
           if(in != null) {
               in.close();
           }
        }
    }


    private void createMapForceServerErrorMailboxEntry(String to, String from, String subject,
                                                       InputStream file, Exchange exchange) throws
            NoSuchHeaderException, BlobStoreException, ValidationException, B2BTransactionFailed,
            B2BNotFoundException {

        String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        Mailbox mailbox = MailboxEntryHelper.getMailbox(mailboxDAO, exchange, "map-server-errors");
        MailboxEntry mailboxEntry = MailboxEntryHelper.createMailboxEntry(exchange, mailbox,
                blobStore, mailboxEntryDAO,to
                ,from, subject, file, UUID.randomUUID() + "-map-server-error");
    }


    private static InputStream doExtractResponseBodyAsStream(InputStream is, Exchange exchange) throws IOException {
        // As httpclient is using a AutoCloseInputStream, it will be closed when the connection is closed
        // we need to cache the stream for it.
        CachedOutputStream cos = null;
        try {
            // This CachedOutputStream will not be closed when the exchange is onCompletion
            cos = new CachedOutputStream(exchange, false);
            IOHelper.copy(is, cos);
            // When the InputStream is closed, the CachedOutputStream will be closed
            return cos.getWrappedInputStream();
        } catch (IOException ex) {
            // try to close the CachedOutputStream when we get the IOException
            try {
                cos.close();
            } catch (IOException ignore) {
                //do nothing here
            }
            throw ex;
        } finally {
            IOHelper.close(is, "Extracting response body", LOG);
        }
    }

}
