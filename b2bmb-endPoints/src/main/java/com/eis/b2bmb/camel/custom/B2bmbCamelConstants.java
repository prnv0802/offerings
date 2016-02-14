package com.eis.b2bmb.camel.custom;

/**
 * @author aaldredge
 */
public abstract class B2bmbCamelConstants {
    /**
     * Transmission Id key for Exchange properties
     */
    public static final String TRANSMISSION_ID = "transmissionId";

    /**
     * To key for Exchange headers
     * @see com.eis.b2bmb.camel.custom.component.B2bmbMailboxEndpoint
     */
    public static final String TO = "to";

    /**
     * From key for Exchange headers
     * @see com.eis.b2bmb.camel.custom.component.B2bmbMailboxEndpoint
     */
    public static final String FROM = "from";

    /**
     * Subject key for Exchange headers
     * @see com.eis.b2bmb.camel.custom.component.B2bmbNotificationEndpoint
     * @see com.eis.b2bmb.camel.custom.component.B2bmbMailboxEndpoint
     */
    public static final String SUBJECT = "subject";

    /**
     * Message body key for Exchange headers
     * @see com.eis.b2bmb.camel.custom.component.B2bmbNotificationEndpoint
     */
    public static final String BODY = "body";

    /**
     * Collection to store shutdown route status
     */
    public static final String RESTART_PERSIST_COLLECTION = "camelRouteShutdownState";

    /**
     * Internal mailbox route used as a routing slip.
     */
    public static final String INTERNAL_MAILBOX_ROUTE = "InternalMailboxRoute";

    /**
     * Dynamic camel route which represents a schema validation check.
     */
    public static final String DYNAMIC_SCHEMA_PATH = "DynamicSchemaPath";

    /**
     * Sequence id to be set in the mailbox entry which represents the control number.
     */
    public static final String MAIL_SEQUENCE_NUMBER= "MailSequenceNumber";

    /**
     * The document type of the file.
     */
    public static final String DOCUMENT_TYPE= "DocumentType";

    /**
     * Indicates if we want the mailbox entry id as the body instead of the message or attachment
     */
    public static final String ID_AS_BODY = "idAsBody";

    /**
     * Indicates if we want to send the mailbox entry id as the body instead of the message or attachment
     */
    public static final String SEND_ID_AS_BODY = "SendIdAsBody";

    /**
     * Indicates if we want the indicates if mail should be sorted by email addresses and
     * sequence number.
     */
    public static final String SORT_MAIL = "sortMail";

    /**
     * Name of Mailbox where 997s that indicate a document was rejected will go.
     */
    public static final String FUNCT_ACK_REJECTED_INDICATOR = "FuncAckRejected";

    /**
     * Comma seperated list of control numbers that will stored with mailbox entry.
     */
    public static final String FUNCT_GROUP_CONTROL_NUMBERS = "FunctionalGroupControlNumbers";

    /**
     * Comma seperated list of control numbers that will stored with mailbox entry.
     */
    public static final String TRANSACTION_CONTROL_NUMBERS = "TransactionControlNumbers";

    /**
     * Mailbox Entry Name.
     */
    public static final String MAILBOX_ENTRY_NAME = "MailboxEntryName";

    /**
     * Map Force Service Name
     */
    public static final String MAP_FORCE_SERVICE_NAME = "MapServiceName";

    /**
     * Map Force Server URL
     */
    public static final String MAP_FORCE_SERVER_URL = "MapForceServerUrl";

    /**
     * Schema name
     */
    public static final String SCHEMA_NAME = "schemaName";

    /**
     * MailboxEntryId
     */
    public static final String MAILBOX_ENTRY_ID = "MailboxEntryId";

    /**
     * Reference Data
     */
    public static final String REFERENCE_DATA = "ReferenceData";

    /**
     * File Type = EDIFACT, X12
     */
    public static final String FILE_TYPE = "FileType";

    /**
     * File Type = EDIFACT
     */
    public static final String FILE_TYPE_EDIFACT = "EDIFACT";

    /**
     * File Type = X12
     */
    public static final String FILE_TYPE_X12 = "X12";

    /**
     * UserAfterEnvelopeMap - 'Y' or 'N' - should we use the AfterEnvelopeMap specfiied in
     * the EDI Profile.
     */
    public static final String USE_AFTER_ENVELOPE_MAP = "UserAfterEnvelopeMap";

    /**
     * Dynamic Route Header for Building Dynamic Routes
     */
    public static final String DYNAMIC_ROUTE = "DynamicRoute";

    /**
     * EDI Profile Header.
     */
    public static final String EDI_PROFILE = "EdiProfile";

    /**
     * Data Domain Header.
     */
    public static final String DATA_DOMAIN = "DataDomain";

    /**
     * Trading Partner Direction - TO OR FROM
     */
    public static final String TRADING_PARTNER_DIRECTION = "TradingPartnerDirection";

    /**
     * Trading Partner Direction - TO
     */
    public static final String TRADING_PARTNER_DIRECTION_TO = "TO";

    /**
     * Trading Partner Direction - FROM
     */
    public static final String TRADING_PARTNER_DIRECTION_FROM = "FROM";

    /**
     * B2B Directory Name - used for Communication Configuration Lookup.
     */
    public static final String B2B_DIRECTORY_NAME = "B2BDirectoryName";

    /**
     * B2B Directory Location - used for Communication Configuration Lookup to specify which
     * directory option from the Communication Configuration - inDirectoryName, outDirectoryName
     * should be used.
     */
    public static final String B2B_DIRECTORY_LOCATION = "B2BDirectoryLocation";

    /**
     * B2B Connection Vendor - used for Communication Configuration Lookup to specify which
     * vendor should be used in the query to look up Communication Configurations - MSI, B2BFTP.
     */
    public static final String B2B_CONNECTION_VENDOR_TYPE = "B2BConnectionVendorType";


    /**
     * B2B Profile Type - MSI or EDI
     */
    public static final String B2B_PROFILE_TYPE = "B2BProfileType";


    /**
     * B2B Profile Type - MSI
     */
    public static final String B2B_PROFILE_TYPE_MSI = "MSI";

    /**
     * B2B Profile Type - EDI
     */
    public static final String B2B_PROFILE_TYPE_EDI = "EDI";

}
