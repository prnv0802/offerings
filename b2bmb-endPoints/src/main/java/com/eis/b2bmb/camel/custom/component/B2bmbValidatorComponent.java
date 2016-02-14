package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.Blob;
import com.eis.core.api.v1.model.BlobStore;
import com.eis.core.api.v1.model.FileSystemEntry;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.component.validator.DefaultLSResourceResolver;
import org.apache.camel.converter.IOConverter;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.camel.processor.validation.ValidatingProcessor;
import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by Envista Tech on 1/6/2015.
 */
public class B2bmbValidatorComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbValidatorComponent.class);

    /**
     * fileSystemEntryDAO - injected by camel
     */
    @Autowired
    protected FileSystemEntryDAO fileSystemEntryDAO;

    /**
     * blobStore - injected by camel
     */
    @Autowired
    protected BlobStore blobStore;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and file path must be in the uri");
        }
        String dataDomain = path.substring(0, path.indexOf('/'));
        String filePath = path.substring(path.indexOf('/') + 1);

        try {
            FileSystemEntry fileSystemEntry = fileSystemEntryDAO.getByRefName(filePath, dataDomain);
            if (fileSystemEntry == null) {
                throw new ResolveEndpointFailedException("Unable to resolve file path with data domain " +
                        dataDomain + " and file path " + filePath);
            }
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new ResolveEndpointFailedException("Unable to resolve file path with data domain " +
                    dataDomain + " and file path " + filePath);
        }
    }

    /**
     * Creates the endpoint.
     *
     * @param uri - String for uri
     * @param path - rest of path
     * @param parameters - parameters
     * @return Endpoint
     * @throws Exception - if endpoint could not be created
     */
    protected Endpoint createEndpoint(String uri, String path, Map<String, Object> parameters) throws Exception {
        FileSystemEntry fileSystemEntry = null;

        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and file path must be in the uri");
        }
        String dataDomain = path.substring(0, path.indexOf('/'));
        String filePath = path.substring(path.indexOf('/') + 1);

        try {
            fileSystemEntry = fileSystemEntryDAO.getByRefName(filePath, dataDomain);
            if (fileSystemEntry == null) {
                throw new ResolveEndpointFailedException("Unable to resolve file path with data domain " +
                        dataDomain + " and file path " + filePath);
            }
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new ResolveEndpointFailedException("Unable to resolve file path with data domain " +
                    dataDomain + " and file path " + filePath);
        }

        Blob fileContents = blobStore.getBlobByStringId(
                fileSystemEntry.getBlobId());

        if (fileContents == null) {
            throw new B2BTransactionFailed("Unable to find blob for id:"+fileSystemEntry.getBlobId());
        }
        InputStream is = fileContents.getInputStream();
        byte[] bytes = null;
        try {
            bytes = IOConverter.toBytes(is);
        } finally {
            // and make sure to close the input stream after the schema has been loaded
            IOHelper.close(is);
        }

        ValidatingProcessor validator = new ValidatingProcessor();
        validator.setSchemaAsByteArray(bytes);
        LOG.debug("{} using schema resource: {}", this, path);
        configureValidator(validator, uri, path, parameters);

        // force loading of schema at create time otherwise concurrent
        // processing could cause thread safe issues for the javax.xml.validation.SchemaFactory
        validator.loadSchema();
        return new ProcessorEndpoint(uri, this, validator);
    }

    /**
     * Configure the validator.
     *
     * @param validator - ValidatingProcessor
     * @param uri - String for the uri
     * @param remaining - the remaining portion of uri
     * @param parameters - the parameters
     * @throws Exception - thrown if the validator could not be configured
     */
    protected void configureValidator(ValidatingProcessor validator, String uri, String remaining,
                                      Map<String, Object> parameters) throws Exception {
        LSResourceResolver resourceResolver = resolveAndRemoveReferenceParameter(
                parameters, "resourceResolver", LSResourceResolver.class);
        if (resourceResolver != null) {
            validator.setResourceResolver(resourceResolver);
        } else {
            validator.setResourceResolver(new DefaultLSResourceResolver(getCamelContext(), remaining));
        }

        setProperties(validator, parameters);
    }
}
