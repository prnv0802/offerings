package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.ExchangedDocumentDAO;
import com.eis.b2bmb.api.v1.model.ExchangedDocument;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.canonical.pub.api.v1.model.*;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.DynamicAttributeType;
import com.eis.core.api.v1.model.DynamicSearchRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aaldredge
 */
public class UpdateFunctionalAckProcessor implements Processor{

    private static final Logger LOG = LoggerFactory.getLogger(UpdateFunctionalAckProcessor.class);


    /**
     *  exchangedDocumentDAO to get exchangedDocument
     */
    @Autowired
    protected ExchangedDocumentDAO exchangedDocumentDAO;

    @Autowired
    Jaxb2Marshaller jaxbMarshaller;


    @Override
    public void process(Exchange exchange) throws Exception {
        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        InputStream inputStream = exchange.getIn().getBody(InputStream.class);

        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);

        ByteArrayOutputStream outputStream = null;
        InputStream vInputStream = null;
        boolean rebuildFuncAck = false;

        try {
            ObjectFactory factory = new ObjectFactory();
            XMLInputFactory xif = javax.xml.stream.XMLInputFactory.newFactory();
            xif.setProperty(javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE, false);
            StreamSource xml = new javax.xml.transform.stream.StreamSource(inputStream);
            XMLStreamReader xsr = xif.createXMLStreamReader(xml);
            jaxbMarshaller.setMappedClass(B2BDocuments.class);

            B2BDocuments b2BDocuments = (B2BDocuments)
                    jaxbMarshaller.unmarshal(new StAXSource(xsr));

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setMarshallerProperties(properties);

            List<B2BFunctionalAck> fas =  b2BDocuments.getB2BFunctionalAck();

            if(fas != null) {

                if (fas.size() == 1) {
                    B2BFunctionalAck fa = fas.get(0);
                    String functionalGroupNumber = fa.getHeader().getHeaderInfo().getGroupControlNumber();

                    TransactionSetResponses transactionSetResponses = fa.getTransactionSetResponses();

                    if (transactionSetResponses != null) {
                        if (transactionSetResponses.getTransactionSetResponse() != null) {
                            if (transactionSetResponses.getTransactionSetResponse().size() == 1) {
                                TransactionSetResponse response =
                                        transactionSetResponses.getTransactionSetResponse().get(0);

                                String transactionControlNumber = response.getTransactionSetControlNumber();
                                String documentId = response.getTransactionSetIdentifierCode();

                                if (functionalGroupNumber != null && transactionControlNumber != null) {
                                    DynamicSearchRequest searchRequest = new DynamicSearchRequest();
                                    searchRequest.setExactMatches(true);
                                    searchRequest.setSearchConditionType("and");

                                    DynamicAttribute daDomain = new DynamicAttribute();
                                    daDomain.setType(DynamicAttributeType.String);
                                    daDomain.setRefName("dataDomain");
                                    daDomain.setValue(dataDomain);
                                    searchRequest.getSearchFields().getAttributes().put("dataDomain", daDomain);

                                    DynamicAttribute daFunctionGroupNumber = new DynamicAttribute();
                                    daFunctionGroupNumber.setType(DynamicAttributeType.String);
                                    daFunctionGroupNumber.setRefName("referenceData.functionalGroupControlNumber");
                                    daFunctionGroupNumber.setValue(functionalGroupNumber);
                                    searchRequest.getSearchFields().getAttributes().put(
                                            "functionalGroupControlNumber", daFunctionGroupNumber);

                                    DynamicAttribute tsControlNumber = new DynamicAttribute();
                                    tsControlNumber.setType(DynamicAttributeType.String);
                                    tsControlNumber.setRefName("referenceData.transactionSetControlNumber");
                                    tsControlNumber.setValue(transactionControlNumber);
                                    searchRequest.getSearchFields().getAttributes().put(
                                            "transactionSetControlNumber", tsControlNumber);

                                    DynamicAttribute daDocType = new DynamicAttribute();
                                    daDocType.setType(DynamicAttributeType.String);
                                    daDocType.setRefName("documentType");
                                    daDocType.setValue(documentId);
                                    searchRequest.getSearchFields().getAttributes().put(
                                            "documentType", daDocType);

                                    ArrayList<String> dataDomains = new ArrayList<String>();
                                    dataDomains.add(dataDomain);

                                    List<ExchangedDocument> exchangedDocumentList =
                                            exchangedDocumentDAO.getList(searchRequest, 0, 20, null, dataDomains);

                                    if (exchangedDocumentList.size() > 0) {
                                        if (exchangedDocumentList.size() == 1) {
                                            ExchangedDocument exchangedDocument = exchangedDocumentList.get(0);

                                            List<String> poNumbers = (List<String>)
                                                    exchangedDocument.getReferenceData().get("poNumber");
                                            if (poNumbers != null) {
                                                String poNumber = poNumbers.get(0);
                                                response.setReferenceNumber(poNumber);
                                                rebuildFuncAck = true;
                                            } else {
                                                if (LOG.isInfoEnabled()) {
                                                    LOG.info("Could not get poNumber for functionGroupControlNumber:" +
                                                            functionalGroupNumber +
                                                            ", transactionSetControlNumber:" +
                                                            transactionControlNumber + ", documentType:" + documentId);
                                                }
                                            }

                                        } else {
                                            if (LOG.isWarnEnabled()) {
                                                LOG.warn("There are more than one exchanged document for" +
                                                        "functionGroupControlNumber:" +
                                                        functionalGroupNumber +
                                                        ", transactionSetControlNumber:" +
                                                        transactionControlNumber + ", documentType:"
                                                        + documentId + "." +
                                                        "Taking the first one found.");
                                            }

                                            ExchangedDocument exchangedDocument = exchangedDocumentList.get(0);

                                            String poNumber = (String)
                                                    exchangedDocument.getReferenceData().get("poNumber");
                                            if (poNumber != null) {
                                                response.setReferenceNumber(poNumber);
                                                rebuildFuncAck = true;
                                            } else {
                                                if (LOG.isInfoEnabled()) {
                                                    LOG.info("Could not get poNumber for functionGroupControlNumber:" +
                                                            functionalGroupNumber +
                                                            ", transactionSetControlNumber:" +
                                                            transactionControlNumber + ", documentType:" + documentId);
                                                }
                                            }
                                        }
                                    } else {
                                        if (LOG.isInfoEnabled()) {
                                            LOG.info("No exchanged documents found for  functionGroupControlNumber:" +
                                                    functionalGroupNumber +
                                                    ", transactionSetControlNumber:" +
                                                    transactionControlNumber + ", documentType:" + documentId);
                                        }
                                    }
                                } else {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn("Functional Group Control Number or Transaction Set Control Number" +
                                                        " not found, can not update the Functional Ack."
                                        );
                                    }
                                }
                            } else {
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn("More than one transaction found.  We can are not equipped to " +
                                            "handle that yet.");
                                }
                            }
                        } else {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("No transaction set response found.");
                            }
                        }
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("No transaction set response found.");
                        }
                    }
                }
            }

            if(rebuildFuncAck) {
                outputStream = new ByteArrayOutputStream();
                jaxbMarshaller.marshal(factory.createB2BDocuments(b2BDocuments),
                        new StreamResult(outputStream));

                byte[] outputBytes = outputStream.toByteArray();
                vInputStream = new ByteArrayInputStream(outputBytes);

                exchange.getIn().setHeader(Exchange.FILE_LENGTH, outputBytes.length);
                exchange.getIn().setBody(vInputStream);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (vInputStream != null) {
                vInputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

}
