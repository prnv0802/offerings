package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.model.EmailMessage;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.camel.component.jms.JmsComponent;
//import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 */

public class EmailProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessor.class);

    @Autowired
    GridFsOperations operations;

    @Override
    public void process(Exchange exchange) throws Exception {

        Message inMessage = exchange.getIn();

        EmailMessage emailMessage = (EmailMessage)inMessage.getBody();

        List<Map<String,String>> lineItemValues = emailMessage.getLineItemValues();

        Map<String, String> templateNameMap = emailMessage.getTemplateNameValues();

        String domain = emailMessage.getDataDomain();
        String templateName = emailMessage.getTemplateName();
        String body = templateNameMap.get("body");
        String contentType = emailMessage.getContentType();

        if (LOG.isInfoEnabled()) {
            LOG.info("Domain  " + domain);
            LOG.info("templateName  " + templateName);
            LOG.info("body  " + body);
        }

        //Get Template from Mongo File System (Grid FS) for the given domain.
        List<GridFSDBFile> result = operations.find(new Query().addCriteria(Criteria.where(
                "filename").is(templateName).and("metadata.dataDomain").is(domain)));
        if (LOG.isInfoEnabled()) {
            LOG.info("result ******** " + result);
        }
        if (result != null && result.size() > 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("No Of Template Files  " + result.size());
            }
            GridFSDBFile file = result.get(0);
            StringWriter writer = new StringWriter();
            VelocityContext context = new VelocityContext();

            //put all the values in InMap to context
            for (String key : templateNameMap.keySet()) {
                context.put(key, templateNameMap.get(key));
            }
            context.put("lineItemList",lineItemValues);

            Velocity.evaluate(context, writer, "Email Template Process",
                    new BufferedReader(new InputStreamReader(file.getInputStream())));


            if (LOG.isInfoEnabled()) {
                LOG.info("Body after applying template  " + writer.toString());
            }

            body = writer.toString();
        }

        // set To, From and Subject Headers
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("To", emailMessage.getToAddress());
        headers.put("From", emailMessage.getFromAddress());
        headers.put("Subject", emailMessage.getSubject());

        exchange.getOut().setHeaders(headers);

        // set body
        exchange.getOut().setBody(body);
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, contentType);

    }
}
