package com.eis.b2bmb.camel.custom.component;

import org.apache.camel.Converter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Custom type converter for HttpEntity
 * @author aaldredge
 */
@Converter
public final class HttpEntityStreamConverter {
    private static final Logger LOG = LoggerFactory.getLogger(HttpEntityStreamConverter.class);

    private HttpEntityStreamConverter() {}

    /**
     * Convert http entity  to input stream
     * @param entity http entity to convert
     * @return InputStream
     */
    @Converter(allowNull = true)
    public static InputStream toInputStream(HttpEntity entity) {
        if (entity == null) {
            return null;
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = null;
            try {
                entity.writeTo(outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                return inputStream;
            } catch(IOException e) {
                IOUtils.closeQuietly(outputStream);
                return null;
            }
        }
    }

    /**
     * Convert blob to string
     * @param entity http entity to convert
     * @return String
     * @throws java.io.IOException if the stream cannot be converted
     */
    @Converter(allowNull = true)
    public static String toString(HttpEntity entity) throws IOException {
        if (entity == null) {
            return null;
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = null;
            try {
                entity.writeTo(outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                return  IOUtils.toString(inputStream);
            } catch(IOException e) {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(inputStream);
                return null;
            }
        }
    }

}
