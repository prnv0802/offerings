package com.eis.b2bmb.camel.custom.processor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.eis.core.api.v1.dao.MercuryGateShipmentDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.model.MercuryGateShipment;

/**
 * User: mingardia
 * Date: 9/25/14 
 * Time: 2:20 PM
 */
public class ShipmentUpdateProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ShipmentUpdateProcessor.class);

    @Autowired
    private MercuryGateShipmentDAO mercuryGateShipmentDAO;

    @Override
    public void process(Exchange exchange) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("<<<<<<<<<<<  ShipmentUpdateProcessor Started : " + this.getClass().getCanonicalName());
        }
        String fileHeader = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        InputStream message = exchange.getIn().getBody(InputStream.class);
        if (message == null) {
            throw new B2BNotFoundException("No stream exist in exchange. ");
        }
        if (fileHeader == null) {
            throw new B2BNotFoundException("No file header exist in exchange. ");
        }
        String[] splitFileHeader = fileHeader.split("\\.");
        String fileName = splitFileHeader[0] + ".txt";

        exchange.getIn().setHeader(Exchange.FILE_NAME, fileName);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/text");

        StringBuilder out = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(message));
        String line;
        long index = 0;

        while ((line = in.readLine()) != null) {
            if (line.trim().equalsIgnoreCase("")) {
                continue;
            }

            if (index > 0) {
                String[] splitChar = line.split(",");
                if (splitChar.length != 3) {
                    // not valid to update
                    out.append("Content not valid to status update for shipment (" + line + ")" + "....\n");
                    continue;
                }
                String[] splitRef = splitChar[0].split(" ");
                String id = splitRef[0];
                String status = splitChar[2];

                List<String> dataDomains = new ArrayList();
                dataDomains.add("com.walmart");

                List<MercuryGateShipment> shipment = mercuryGateShipmentDAO.getListByIdRegEx(id, 0, -1, null,
                        dataDomains);

                if (shipment == null || shipment.isEmpty()) {
                    // not valid shipment
                    out.append("Shipment not exist for shipment id " + id + "....\n");
                    continue;
                }
                shipment.get(0).setStatus(status);
                mercuryGateShipmentDAO.save(shipment.get(0));
                out.append("Shipment Id " + id + " has been updated successfully...." + "\n");
            }
            line = "";
            index++;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("<<<<<<<<<<<  ShipmentUpdateProcessor Ended : " + this.getClass().getCanonicalName());
        }

        exchange.getIn().setBody(out.toString());
    }
}
