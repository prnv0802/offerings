package com.eis.b2bmb.util;

import com.eis.b2bmb.api.v1.model.CommunicationProtocol;
import com.eis.common.Constants;
import com.eis.core.api.v1.dao.TransmissionDAO;
import com.eis.core.api.v1.dao.TransmissionEventDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Map;

/**
 * Class that records transmissions during AS2 events.
 */
public class TransmissionRecorder {

    @Autowired
    private TransmissionDAO transmissionDAO;

    @Autowired
    private TransmissionEventDAO transmissionEventDAO;

    /**
     * Creates a Transmission object with AS2 Information.
     *
     * @param direction - Transmission Direction - inbound or outbound
     * @param dataDomain - the data domain
     * @param communicationProtocol - SFTP, HTTP, AS2
     * @param from - who the file is from
     * @param to - who the file it to
     * @param fileName - the fileName
     * @param objectType - the type of object used in the transmission
     * @return Transmission object
     */
    public Transmission createTransmission(TransmissionDirection direction,String dataDomain,
                                           CommunicationProtocol communicationProtocol,
                                           String from, String to, String fileName, String objectType){
        return createTransmission(direction, dataDomain, communicationProtocol, from, to, fileName, objectType, null,
                null, null);
    }

    /**
     * Creates a Transmission object with AS2 Information.
     *
     * @param direction - Transmission Direction - inbound or outbound
     * @param dataDomain - the data domain
     * @param communicationProtocol - SFTP, HTTP, AS2
     * @param from - who the file is from
     * @param to - who the file it to
     * @param fileName - the fileName
     * @param objectType - the type of object used in the transmission
     * @param connectionId - Unique Id of a particular connection in SFTP, HTTP, AS2
     * @return Transmission object
     */
    public Transmission createTransmission(TransmissionDirection direction,String dataDomain,
                                           CommunicationProtocol communicationProtocol,
                                           String from, String to, String fileName, String objectType,
                                           String connectionId){
        return createTransmission(direction, dataDomain, communicationProtocol, from, to, fileName, objectType,
                connectionId, null, null);
    }

    /**
     * Creates a Transmission object with AS2 Information.
     *
     * @param direction - Transmission Direction - inbound or outbound
     * @param dataDomain - the data domain
     * @param communicationProtocol - SFTP, HTTP, AS2
     * @param from - who the file is from
     * @param to - who the file it to
     * @param fileName - the fileName
     * @param objectType - the type of object used in the transmission
     * @param remoteHost - IP / hostname of the remote party (sender / receiver)
     * @param remotePort - client / server port of the remote party
     * @return Transmission object
     */
    public Transmission createTransmission(TransmissionDirection direction,String dataDomain,
                                           CommunicationProtocol communicationProtocol,
                                           String from, String to, String fileName, String objectType,
                                           String remoteHost, String remotePort){
        return createTransmission(direction, dataDomain, communicationProtocol, from, to, fileName, objectType, null,
                remoteHost, remotePort);
    }

    /**
     * Creates a Transmission object with AS2 Information.
     *
     * @param direction - Transmission Direction - inbound or outbound
     * @param dataDomain - the data domain
     * @param communicationProtocol - SFTP, HTTP, AS2
     * @param from - who the file is from
     * @param to - who the file it to
     * @param fileName - the fileName
     * @param objectType - the type of object used in the transmission
     * @param connectionId - Unique Id of a particular connection in SFTP, HTTP, AS2
     * @param remoteHost - IP / hostname of the remote party (sender / receiver)
     * @param remotePort - client / server port of the remote party
     * @return Transmission object
     */
    public Transmission createTransmission(TransmissionDirection direction,String dataDomain,
                                           CommunicationProtocol communicationProtocol,
                                           String from, String to, String fileName, String objectType,
                                           String connectionId, String remoteHost, String remotePort){

        if (direction == null)
        {
            throw new IllegalArgumentException("Direction is required to be non null and no empty");
        }

        if (dataDomain == null)
        {
            throw new IllegalArgumentException("data domain is required");
        }

        if (communicationProtocol == null)
        {
            throw new IllegalArgumentException(" communication protocol can not be null");
        }

        if (from == null)
        {
            throw new IllegalArgumentException(" from can not be null");
        }

        if ( to == null)
        {
            throw new IllegalArgumentException(" to can not be null");
        }

        if ( fileName == null )
        {
            throw new IllegalArgumentException( "fileName can not be null");
        }

        Transmission transmission = new Transmission();
        try {

            //this would be very weird but maybe you preset the id in the route so supporting
            transmission.setId(ObjectId.get().toHexString());
            transmission.setDataDomain(dataDomain);
            transmission.setDirection(direction);
            transmission.setStartDateTime(Calendar.getInstance().getTime());
            if (fileName != null) {
                transmission.setFileNames(fileName);
            } else {
                transmission.setFileNames("Unknown");
            }

            transmission.setTransmissionObjectIdType(objectType);

            transmission.setCommunicationProtocol(communicationProtocol.toString());
            transmission.setStatus(TransmissionStatus.NEW);
            transmission.setFromUser(from);
            transmission.setToUser(to);
            transmission.setConnectionId(connectionId);
            transmission.setRemoteHost(remoteHost);
            transmission.setRemotePort(remotePort);
            transmission = transmissionDAO.save(transmission);
            return transmission;
        } catch (B2BNotFoundException e) {
            e.printStackTrace();
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            b2BTransactionFailed.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        }

        return transmission;

    }

    /**
     * Creates a TransmissionEvent and saves it for the the Transmission.
     *
     * @param transmission  - the transmission object
     * @param communicationProtocol - SFTP, HTTP, AS2    *
     * @param data - map of data to save with the transmission
     * @param dataDomain - dataDomain to save in
     * @param status - the TransmissionStatus
     * @param eventType - event information
     * @param from - who the file is from
     * @param to - who the file it to
     * @param fileName - the fileName
     * @param objectType - the type of object used in the transmission*
     * @param direction - Transmission Direction - inbound or outbound
     */
    public void addTransmissionEvent(Transmission transmission,  CommunicationProtocol communicationProtocol,
                                     Map<String, Object> data, String dataDomain, TransmissionStatus status,
                                     String eventType, String from, String to, String fileName,String objectType,
                                     TransmissionDirection direction) {
        try {

            if(transmission == null) {
                transmission = createTransmission(direction,  Constants.CANTATA_APP_DATADOMAIN,
                        communicationProtocol,
                        from, to, fileName, objectType);
            }

            if (!transmission.getDataDomain().equals(dataDomain)) {
                transmission.getDataDomains().add(dataDomain);
            }
            TransmissionEvent transmissionEvent = new TransmissionEvent();
            transmissionEvent.setId((ObjectId.get().toHexString()));
            transmissionEvent.setStartDateTime(Calendar.getInstance().getTime());
            transmissionEvent.setTimestamp(System.nanoTime());


            transmissionEvent.setDataDomain(transmission.getDataDomain());
            if (!transmissionEvent.getDataDomain().equals(dataDomain)) {
                transmissionEvent.getDataDomains().add(dataDomain);
            }

            transmissionEvent.setTransmissionId(transmission.getId());
            DynamicAttributeSet das = new DynamicAttributeSet();

            for (String headerKey : data.keySet()) {

                DynamicAttribute headerAttribute = new DynamicAttribute();
                headerAttribute.setType(DynamicAttributeType.Text);
                headerAttribute.setLabel(headerKey + ":");
                headerAttribute.setRequired(false);
                headerAttribute.setRefName(headerKey);
                headerAttribute.setValue(data.get(headerKey));
                das.getAttributes().put(headerKey, headerAttribute);

            }

            transmissionEvent.getAttributes().getAttributes().putAll(das.getAttributes());
            transmissionEvent.setEventType(eventType);
            transmissionEvent.setRefName(transmissionEvent.createRefName(transmission));
            transmissionEventDAO.save(transmissionEvent);

            if (!transmission.getStatus().equals(status)) {
                DynamicAttribute statusChange = new DynamicAttribute();
                statusChange.setType(DynamicAttributeType.Text);
                statusChange.setLabel("Status Changed:");
                statusChange.setRequired(false);
                statusChange.setRefName("statusChange");
                statusChange.setValue(status.toString());
                das.getAttributes().put("statusChange", statusChange);
                transmission.setStatus(status);
            }
            transmissionDAO.save(transmission);
        } catch (B2BNotFoundException e) {
            e.printStackTrace();
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            b2BTransactionFailed.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
}
