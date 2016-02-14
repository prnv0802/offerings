package com.eis.b2bmb.camel.custom.util;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Envista Tech on 5/29/2015.
 */
public class ConvertToAttachmentBean {

    /**
     * Process method which will convert the body of the exchange into an attachment.
     *
     * @param exchange - Camel Exchange
     * @throws java.io.IOException - if the file can not be written
     */
    public void process(Exchange exchange) throws IOException{
        Message in = exchange.getIn();
        InputStream message = exchange.getIn().getBody(InputStream.class);

        String fileId = in.getHeader("CamelFileName",String.class);

        exchange.getIn().setBody("");
        String to = exchange.getIn().getHeader(B2bmbCamelConstants.TO, String.class);
        String from = exchange.getIn().getHeader(B2bmbCamelConstants.FROM, String.class);
        String subject = exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(B2bmbCamelConstants.TO, to);
        headers.put(B2bmbCamelConstants.FROM, from);
        headers.put(B2bmbCamelConstants.SUBJECT, subject);
        exchange.getIn().addAttachment(fileId, new DataHandler(new ByteArrayDataSource(IOUtils.toByteArray(message),
                "application/octet-stream")));
        exchange.getIn().setHeaders(headers);

    }
}