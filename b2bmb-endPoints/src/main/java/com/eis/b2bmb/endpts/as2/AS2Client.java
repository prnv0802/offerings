package com.eis.b2bmb.endpts.as2;

import com.eis.b2bmb.api.v1.model.AS2ClientConnectionConfig;
import com.eis.core.api.v1.exception.B2BTransactionFailed;

import java.io.InputStream;

/**
 * User: mingardia
 * Date: 11/16/14
 * Time: 9:12 AM
 */
public interface AS2Client {

    /**
     * Sends the given file with the configuration
     * @param config the configuration
     * @param dataPayloadName - the name of the payload to use
     * @param dataStreamToSend - the inputStream to pull data data from
     * @param transmissionId = the transmissionid we are sending this in
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed - the transaction was not successful
     */
    public void sendFile(AS2ClientConnectionConfig config,
                         String dataPayloadName, InputStream dataStreamToSend, String transmissionId)
            throws B2BTransactionFailed;
}
