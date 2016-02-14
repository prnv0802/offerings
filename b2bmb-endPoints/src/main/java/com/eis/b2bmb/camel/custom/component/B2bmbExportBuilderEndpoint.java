
package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.objectbuilders.JAXBExporter;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author harjeets
 */
public class B2bmbExportBuilderEndpoint extends DefaultPollingEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(B2bmbExportBuilderEndpoint.class);
    private boolean exportAll;
    private String domain;
    private JAXBExporter exporter;
    private String exportName;

    /**
     * @param endpointUri endpoint Uri
     * @param component   component instance
     */
    public B2bmbExportBuilderEndpoint(String endpointUri, B2bmbExportBuilderComponent component) {
        super(endpointUri, component);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating B2bmbExportBuilderEndpoint  ");
        }
    }

    /**
     * creates the producer object
     *
     * @return {@link org.apache.camel.Producer}
     * @throws Exception thrown by camel runtime system
     */
    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("You cannot send messages to this endpoint");

    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        B2bmbExportBuilderConsumer consumer = new B2bmbExportBuilderConsumer(this, processor);

        // ScheduledPollConsumer default delay is 500 millis, override with a new default value.
        // End user can override this value by providing a consumer.delay parameter
        consumer.setDelay(consumer.DEFAULT_CONSUMER_DELAY);
        configureConsumer(consumer);
        return consumer;
    }

    /**
     * get exportAll
     * @return exportAll
     */
    public boolean isExportAll() {
        return exportAll;
    }

    /**
     * set exportAll
     * @param exportAll exportAll
     */
    public void setExportAll(boolean exportAll) {
        this.exportAll = exportAll;
    }

    /**
     * Get domain
     * @return domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set domain
     * @param domain dataDomain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Set exporter
     * @param exporter exporter
     */
    public void setExporter(JAXBExporter exporter) {
        this.exporter = exporter;
    }

    /**
     * Get exporter
     * @return exporter
     */
    public JAXBExporter getExporter() {
        return exporter;
    }

    /**
     * Get export name
     * @return export name
     */
    public String getExportName() {
        return exportName;
    }

    /**
     * Set export name
     * @param exportName export name
     */
    public void setExportName(String exportName) {
        this.exportName = exportName;
    }

    /**
     * to create singleton  object of endpoint
     *
     * @return boolean
     */

    public boolean isSingleton() {
        return true;
    }

    @Override
    public String toString() {
        return "B2bmbExportBuilderEndpoint{" +
                domain + " "
                + exportName +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        B2bmbExportBuilderEndpoint that = (B2bmbExportBuilderEndpoint) o;

        if (exportAll != that.exportAll) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (exportName != null ? !exportName.equals(that.exportName) : that.exportName != null) return false;
        if (exporter != null ? !exporter.equals(that.exporter) : that.exporter != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (exportAll ? 1 : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (exporter != null ? exporter.hashCode() : 0);
        result = 31 * result + (exportName != null ? exportName.hashCode() : 0);
        return result;
    }
}
