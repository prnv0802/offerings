package com.eis.b2bmb.endpts.as2.server;


public class TestAs2Client {

}

/**
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/META-INF/springContext.xml" })
 public class TestAs2Client extends TestBase {


 private static final Logger LOG = LoggerFactory
 .getLogger(TestAs2Client.class);

 @Autowired AS2Client as2Client;
 @Autowired AS2RelationShipEntryDAO entryDAO;

 @Autowired ExchangeDefinitionDAO definitionDAO;

 @Autowired MailboxDAO mailboxDAO;

 @Autowired MailboxEntryDAO mailboxEntryDAO;

 @Test public void dummyTest() {
 LOG.debug("Dummy test >>>> ");
 }

 // @Test
 public void test() throws B2BNotAuthorizedException, B2BNotAuthenticatedException, B2BTransactionFailed,
 InEDIException, B2BNotFoundException, CertificateException, IOException, ValidationException {
 try {

 if (LOG.isDebugEnabled()) {
 LOG.debug("Server side login>>>>>>");
 }

 serverSideLogin();

 String senderAS2ID = "PerformanceBikesAS2";
 String receiverAs2ID = "ShimanoAS2";
 String mailboxRefname = "Inbox";
 String dataDomainPBikes = "com.pbkies";
 String dataDomain = "com.mycompanyxyz";
 List<EDIAttachment> attList = new ArrayList<EDIAttachment>();

 String fileName = "as2TestData/INOVIS_11020_850-";

 for (int i = 1; i < 4; i++) {

 FileInputStream inputStream = null;
 try {
 inputStream = new FileInputStream(fileName + i + ".x12");
 } catch (FileNotFoundException e) {
 fail("EDI File not found  !!!!!" + e);
 }

 if (inputStream != null) {

 LOG.debug("Setting input stream !!!!!!!!!!!!   for file name ::"
 + fileName + i + ".x12");
 EDIAttachment ediAttachment = new EDIAttachment();
 ediAttachment.setContentType("application/edi-x12");
 ediAttachment.setInputStream(inputStream);
 ediAttachment.setFilename(fileName + i + ".x12");
 ediAttachment.setName(fileName + i + ".x12");
 ediAttachment.setHeaders(fileName + i + ".x12");
 attList.add(ediAttachment);

 } else {
 LOG.error("inputStream found null  !!!!!!!!!");
 }

 }

 MDNReceipt mdnReceipt = null;

 AS2RelationShipEntry shimonaToPb = entryDAO.getByFromAndTo(
 senderAS2ID, receiverAs2ID);

 if (LOG.isDebugEnabled()) {
 LOG.debug("Relation Ship found  ::" + shimonaToPb.toString());
 }

 if (shimonaToPb != null) {
 ExchangeDefinition exchangeDefinition = definitionDAO
 .getByRefName(shimonaToPb.getExchangeDefRefName(),
 shimonaToPb.getExchangeDefDataDomain());

 if (exchangeDefinition != null) {
 // setting edi file to be sent AS2 server
 fileName = "src/test/resources/as2TestResources/EdiTestFile.x12";

 if (LOG.isDebugEnabled()) {
 LOG.debug("getting file from file system with name :"
 + fileName);
 }
 // setting edi file to be sent AS2 server
 EDIData ediData = new EDIData(fileName,
 "application/edi-x12");
 String mailBoxRefName="testMailbox2";
 Mailbox mb=mailboxDAO.getByRefName(mailBoxRefName, dataDomain);
 if(mb==null){
 mb = new Mailbox();
 mb.setId(String.valueOf(UUID.randomUUID()));
 mb.setRefName(mailBoxRefName);
 mb.setDataDomain(dataDomain);
 mb.setMailboxRole(MailboxRole.INTERMEDIATE);
 mb.setSystemMailbox(false);
 mb.setPinned(false);
 mailboxDAO.save(mb);
 }

 String mailBoxEntryRefName = "entryRef5";
 MailboxEntry me = mailboxEntryDAO.getByRefName(mailBoxEntryRefName, dataDomain);
 if(me==null){
 me = new MailboxEntry();
 me.setId(String.valueOf(UUID.randomUUID()));
 me.setMailboxId(mb.getId());
 me.setFromUserId("mingardia@mycompanyxyz.com");
 me.setToUserId("mingardia@mycompanyxyz.com");
 me.setSubject("Test Message");
 me.setRefName(mailBoxEntryRefName);
 me.setDataDomain(dataDomain);
 mailboxEntryDAO.save(me);
 }
 mdnReceipt = as2Client.postDataToServer(exchangeDefinition,
 ediData, attList, shimonaToPb.getFromOrgRef(), me.getId());

 } else {
 fail("ExchangeDefinition  found null !!!!!!!!");
 }

 } else {
 fail(" Relationship not found for sender :::" + senderAS2ID
 + " and receiver ::" + receiverAs2ID);
 }

 if (LOG.isDebugEnabled()) {

 LOG.debug(" mdnReceipt received with following detail>>>>>>>> ::::::::: ");
 LOG.debug(" mdnReceipt getHeaders() " + mdnReceipt.getHeaders());
 LOG.debug(" mdnReceipt.getContent()  "
 + new String(mdnReceipt.getContent()));

 LOG.debug(" mdnReceipt.getMessage() " + mdnReceipt.getMessage());
 LOG.debug(" mdnReceipt.getMDN() " + mdnReceipt.getMDN());

 }

 if (LOG.isDebugEnabled()) {
 LOG.debug("data posted method called");
 }

 }  finally {
 logout();
 }
 }


 public AS2Client getAs2Client() {
 return as2Client;
 }


 public void setAs2Client(AS2Client as2Client) {
 this.as2Client = as2Client;
 }

 }  **/
