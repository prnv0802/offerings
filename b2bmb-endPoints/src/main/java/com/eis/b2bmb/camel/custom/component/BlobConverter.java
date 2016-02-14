package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.model.Blob;
import org.apache.camel.Converter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Custom type converter for Blob
 * @author aaldredge
 */
@Converter
public final class BlobConverter {
    private static final Logger LOG = LoggerFactory.getLogger(BlobConverter.class);

    private BlobConverter() {}

    /**
     * Convert blob to input stream
     * @param blob blob to convert
     * @return InputStream
     */
    @Converter(allowNull = true)
    public static InputStream toInputStream(Blob blob) {
        if (blob == null) {
            return null;
        } else {
            return blob.getInputStream();
        }
    }

    /**
     * Convert blob to string
     * @param blob blob to convert
     * @return String
     * @throws IOException if the stream cannot be converted
     */
    @Converter(allowNull = true)
    public static String toString(Blob blob) throws IOException {
        if (blob == null) {
            return null;
        } else {
            return IOUtils.toString(blob.getInputStream());
        }
    }

}
