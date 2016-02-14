package com.eis.b2bmb.camel.custom.util;

import com.eis.b2bmb.api.v1.exception.MailboxRouterNotFoundException;
import com.eis.b2bmb.api.v1.model.MailboxRouter;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.router.MailboxRouterHelper;
import com.eis.spring.util.SpringApplicationContext;
import inedi.InEDIException;
import org.apache.camel.Exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

/**
 * Utility class used to figure out what type of document is being routed through the system.
 */
public class DocumentTypeHelper {

    /**
     * Attempts to figure out what the document type of the file is (850, 856, etc) by looking at the
     * Headers in the exchange.  If the Document Type is passed in the header it will use that, but if
     * it is not found will attempt to figure out the document type from the file name.
     *
     * @param exchange - Camel Exchange
     * @return String identifying the document type                  I
     * @throws com.eis.b2bmb.api.v1.exception.MailboxRouterNotFoundException - if the mailbox router can not be found
     * @throws java.io.IOException - if the file can not be read
     */
    public static String getDocumentType(Exchange exchange) throws MailboxRouterNotFoundException, IOException {
        String docType = exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class);
        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);
        if(docType == null && fileName != null) {
            if (fileName.contains("_850") || fileName.contains("850_") || fileName.contains("B2B Purchase Order")) {
                if ("X12".equals(fileType)) {
                    docType = "850";
                } else if ("EDIFACT".equals(fileType)) {
                    docType = "ORDERS";
                }
            } else if (fileName.contains("_810") || fileName.contains("810_") || fileName.contains("B2B Invoice")) {
                if ("X12".equals(fileType)) {
                    docType = "810";
                } else if ("EDIFACT".equals(fileType)) {
                    docType = "INVOIC";
                }
            } else if (fileName.contains("_856") || fileName.contains("856_") || fileName.contains("B2B ASN")) {
                if ("X12".equals(fileType)) {
                    docType = "856";
                } else if ("EDIFACT".equals(fileType)) {
                    docType = "DESADV";
                }
            } else if (fileName.contains("_846") || fileName.contains("846_") ||
                    fileName.contains("B2B Inventory Inquiry")) {
                if ("X12".equals(fileType)) {
                    docType = "846";
                } else if ("EDIFACT".equals(fileType)) {
                    docType = "INVRPT";
                }
            } else if (fileName.contains("_855") || fileName.contains("855_") || fileName.contains("B2B PO Ack")) {
                if ("X12".equals(fileType)) {
                    docType = "855";
                } else if ("EDIFACT".equals(fileType)) {
                    docType = "ORDRSP";
                }
            } else if (fileName.contains("_997") || fileName.contains("997_")) {
                docType = "997";
            } else if (fileName.contains("_820") || fileName.contains("820_") || fileName.contains(
                    "B2B Payment Advice")) {
                docType = "820";
            } else if (fileName.contains("_858") || fileName.contains("858_") || fileName.contains(
                    "B2B Shipment Information")) {
                docType = "858";
            } else if (fileName.contains("_211") || fileName.contains("211_") ||
                    fileName.contains("B2B Bill of Lading")) {
                docType = "211";

            } else if (fileName.contains("_204") || fileName.contains("204_") ||
                    fileName.contains("B2B Load Tender")) {
                docType = "204";

            } else if (fileName.contains("_210") || fileName.contains("210_") ||
                    fileName.contains("B2B Freight Invoice")) {
                docType = "210";

            } else if (fileName.contains("_214") || fileName.contains("214_") ||
                    fileName.contains("B2B Shipment Status")) {
                docType = "214";

            } else if (fileName.contains("_753") || fileName.contains("753_") ||
                    fileName.contains("B2B Request For Routing Instructions")) {
                docType = "753";
            } else if (fileName.contains("_754") || fileName.contains("754_") ||
                    fileName.contains("B2B Routing Instructions")) {
                docType = "754";
            } else if (fileName.contains("_812") || fileName.contains("812_") ||
                    fileName.contains("B2B Credit Debit Adjustment")) {
                docType = "812";
            } else if (fileName.contains("_830") || fileName.contains("830_") ||
                    fileName.contains("B2B Planning Schedule")) {
                docType = "830";
            } else if (fileName.contains("_832") || fileName.contains("832_") ||
                    fileName.contains("B2B Sales Catalog")) {
                docType = "832";
            } else if (fileName.contains("_852") || fileName.contains("852_") ||
                    fileName.contains("B2B Product Activity")) {
                if ("X12".equals(fileType)) {
                    docType = "852";
                } else if ("EDIFACT".equals(fileType)) {
                    docType = "SLSRPT";
                }
            } else if (fileName.contains("_860") || fileName.contains("860_") ||
                    fileName.contains("B2B PO Change")) {
                docType = "860";
            } else if (fileName.contains("_861") || fileName.contains("861_") ||
                    fileName.contains("B2B Receiving Advice")) {
                docType = "861";

            } else if (fileName.contains("_865") || fileName.contains("865_") ||
                    fileName.contains("B2B PO Change Ack")) {
                docType = "865";

            } else if (fileName.contains("_870") || fileName.contains("870_") ||
                    fileName.contains("B2B Order Status")) {
                docType = "870";
            } else if (fileName.contains("_990") || fileName.contains("990_") ||
                    fileName.contains("B2B Load Tender Response")) {
                docType = "990";

            } else if (fileName.contains("_IFCSUM") || fileName.contains("IFCSUM_") ||
                    fileName.contains("B2B Forwarding Consolidation Summary")) {
                docType = "IFCSUM";
            } else if (fileName.contains("_180") || fileName.contains("180_") ||
                    fileName.contains("B2B Return Merchandise Authorization")) {
                docType = "180";
            } else if (fileName.contains("_816") || fileName.contains("816_") ||
                    fileName.contains("B2B Org Relationships")) {
                docType = "816";
            } else if (fileName.contains("_888") || fileName.contains("888_") ||
                    fileName.contains("B2B Item Maintenance")) {
                docType = "888";
            } else if (fileName.contains("_940") || fileName.contains("940_") ||
                fileName.contains("B2B Warehouse Shipping Order")) {
                docType = "940";

            } else if (fileName.contains("_943") || fileName.contains("943_") ||
                fileName.contains("B2B Warehouse Stock Transfer Shipment Advice")) {
                docType = "943";
            } else if (fileName.contains("_944") || fileName.contains("944_") ||
                fileName.contains("B2B Warehouse Stock Transfer Receipt Advice")) {
                docType = "944";

            } else if (fileName.contains("_945") || fileName.contains("945_") ||
                fileName.contains("B2B Warehouse Shipping Advice")) {
                docType = "945";

            } else if (fileName.contains("_947") || fileName.contains("947_") ||
                fileName.contains("B2B Warehouse Inventory Adjustment Advice")) {
                docType = "947";
            }

            if (docType == null) {
                InputStream message = exchange.getIn().getBody(InputStream.class);

                if (fileType == null) {
                    fileType = getFileType(exchange);
                }
                if (fileName.toUpperCase().endsWith(".X12") || fileName.toUpperCase().endsWith(".EDI")) {
                    MailboxRouterHelper mailboxRouterHelper = (MailboxRouterHelper)
                            SpringApplicationContext.getBean("mailboxRoutingHelper");
                    MailboxRouter mailboxRouter = null;
                    try {
                        mailboxRouter = mailboxRouterHelper.getMailboxRouterFromFile(message,
                                fileName, fileType);
                    } catch (InEDIException e) {
                        throw new MailboxRouterNotFoundException("Error getting mailbox router, could not determine" +
                                " document type", e);
                    } catch (B2BNotFoundException e) {
                        throw new MailboxRouterNotFoundException("Error getting mailbox router, could not determine" +
                                " document type", e);
                    } catch (B2BTransactionFailed e) {
                        throw new MailboxRouterNotFoundException("Error getting mailbox router, could not determine" +
                                " document type", e);
                    } catch (TooManyListenersException e) {
                        throw new MailboxRouterNotFoundException("Error getting mailbox router, could not determine" +
                                " document type", e);
                    }
                    if (mailboxRouter != null && mailboxRouterHelper != null) {
                        docType = mailboxRouterHelper.getDocumentNumber();
                    }
                }
            }

        }
        return docType;
    }

    /**
     * Returns the fileType 'EDIFACT' or 'X12' based on reading the first line of the file.
     *
     * @param exchange - Exchange object
     * @return String containing the fileType
     * @throws IOException - if the file can not be read
     */
    public static String getFileType(Exchange exchange) throws IOException{
        String fileType = null;
        InputStream message = exchange.getIn().getBody(InputStream.class);
        if (message != null) {
            InputStreamReader is = new InputStreamReader(message);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(is);
            String read = br.readLine();
            if (read != null) {
                if (read.startsWith("ISA")) {
                    fileType = "X12";
                } else if (read.startsWith("UNB")) {
                    fileType = "EDIFACT";
                }
            }
        }
        return fileType;
    }
}
