package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.objectbuilders.JAXBExporter;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Date;

/**
 * @author aaldredge
 */
public class B2bmbExportBuilderConsumer extends ScheduledPollConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbExportBuilderConsumer.class);

    //package visibility, default delay if none set
    static final long DEFAULT_CONSUMER_DELAY = 60 * 60 * 1000L;

    /**
     * Parameterized  constuctor
     *
     * @param endpoint   endpoint instance
     * @param processor  processor instance
     */
    public B2bmbExportBuilderConsumer(B2bmbExportBuilderEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    protected int poll() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Calling  B2bmbExportBuilderConsumer   poll() method ");
        }
        if (getEndpoint().getExporter() != null) {
            boolean populatedExchange = false;
            Exchange exchange = getEndpoint().createExchange();

            JAXBExporter exporter = getEndpoint().getExporter();
            InputStream inputStream =  exporter.buildExportXml(getEndpoint().getDomain(), getEndpoint().isExportAll());
            if (inputStream != null) {
                //todo: make more sophisticated w/ call back for success and failure and export sets; maybe
                //batching to handle large data sets
                exchange.getIn().setBody(inputStream, InputStream.class);
                exchange.getIn().setHeader(Exchange.FILE_NAME, getEndpoint().getExportName() + "_"
                        + (getEndpoint().isExportAll()?"full":"delta") + "_"
                        + DateFormatUtils.format(new Date(), "yyyy-MM-dd_HH:mm:SS") + ".xml");
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/xml");
                getProcessor().process(exchange);
                return 1;
            } else {
                return 0;
            }
        } else {
            throw new B2BNotFoundException("Exporter was not found.");
        }
    }

    @Override
    public B2bmbExportBuilderEndpoint getEndpoint() {
        return (B2bmbExportBuilderEndpoint) super.getEndpoint();
    }

    @Override
    public String toString() {
        return "B2bmbExportBuilderConsumer";
    }
}
