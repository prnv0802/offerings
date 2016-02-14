<html>
<head>
<title>EDI Integrator V9 Demos - RosettaNet Server</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="EDI Integrator V9 Demos - RosettaNet Server"></head>

<body>
<div id="content">
<h1>EDI Integrator - Demo Pages</h1>
<h2>RosettaNet Server</h2>
<p>A simple example of a RosettaNet server.</p>
<a href="seecode.jsp?rnifserver.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="inedi.Certificate"%>
<%@ page import="inedi.Rnifreceiver"%>
<%

if (request.getMethod().equalsIgnoreCase("POST")) {
  Rnifreceiver rnifReceiver = new Rnifreceiver();

  try {
    rnifReceiver.config("AttachmentOutputPath=\"..\\\"");

    rnifReceiver.readRequest(request);
    rnifReceiver.parseHeaders();

    //You can specify your encryption certificate here to allow the receiver
    //to automatically decrypt data. You must set the certificate before calling
    //parse request.
    rnifReceiver.setCertificate(new Certificate(Certificate.cstPFXFile, "RNIFReceiver.pfx", "test", "RNIF Test Receiving Organization"));

    rnifReceiver.setSignerCert(new Certificate("RNIFSender.cer"));

    rnifReceiver.parseRequest();

    //Once the request has been parsed, you can process the service content PIP.
    //After it has been processed, you can respond with a receipt acknowledgement
    //or another business action.

    //If the request asks for an asynchronous response, or if one is dictated
    //by the nature of the PIP, you should send the response out-of-band using
    //the Rnifsender.

    if (rnifReceiver.getResponseType() == Rnifreceiver.rtAsync)
      return;

    //First, reset the receiver to clear the header values before creating
    //a new RosettaNet message.
    rnifReceiver.reset();

    //Now set the headers to identify your business and role in the currentsigned receipt makes sense 
    //transaction as well as provide information about the outgoing message.

    rnifReceiver.setStandardName("RosettaNet");
    rnifReceiver.setStandardVersion("V02.00");

    rnifReceiver.setSecureTransportRequired(true);
    rnifReceiver.setMessageDateTime("20001121T145200.000Z");
    rnifReceiver.setMessageSenderId("12334566");
    rnifReceiver.setMessageReceiverId("000000002");
    rnifReceiver.setMessageTrackingId("" + System.currentTimeMillis());

    rnifReceiver.setFromRole("Seller");
    rnifReceiver.setFromService("Seller Service");
    rnifReceiver.setReplyMessage(true);
    rnifReceiver.setToRole("Buyer");
    rnifReceiver.setToService("Buyer Service");
    rnifReceiver.getAttachments().clear();
    rnifReceiver.setActionMessage(false);
    rnifReceiver.setActionCode("Acknowledement");
    rnifReceiver.setSignalMessage(true);
    rnifReceiver.setGlobalUsageCode(Rnifreceiver.gucProduction);
    rnifReceiver.setPIPCode("3B2");
    rnifReceiver.setPIPInstanceId("11234567");
    rnifReceiver.setPIPVersion("1.2");
    rnifReceiver.getQOSSpecifications().clear();
    rnifReceiver.setPartnerKnown(true);
    rnifReceiver.setPartnerId("000002122");

    rnifReceiver.setServiceContent("<ReceiptAcknowledgement/>");

    //If you want to encrypt the response, set the recipient cert before calling
    //sendResponse. You can use Certmgr to read in a public key certificate.
    
    //rnifReceiver.setRecipientCert(new Certificate(RNIFSender.cer));
    //rnifReceiver.setEncryptionType(Rnifreceiver.etEncryptServiceContent);

    //To digitally sign the response, you must specify a private key. 

    rnifReceiver.setCertificate(new Certificate(Certificate.cstPFXFile, "RNIFReceiver.pfx", "test", "RNIF Test Receiving Organization"));

    rnifReceiver.sendResponse(response);
  } catch (Exception ex) {
    out.write(ex.getMessage());
  }
}

%>
<br/>
<br/>
<br/>
<hr/>
NOTE: These pages are simple demos, and by no means complete applications.  They
are intended to illustrate the usage of the EDI Integrator objects in a simple,
straightforward way.  What we are hoping to demonstrate is how simple it is to
program with our components.  If you want to know more about them, or if you have
questions, please visit <a href="http://www.nsoftware.com/?demopg-BEEDI Integrator9V" target="_blank">www.nsoftware.com</a> or
contact our technical <a href="http://www.nsoftware.com/support/">support</a>.
<br/>
<br/>
Copyright (c) 2013 /n software inc. - All rights reserved.
<br/>
<br/></div>

<div id="footer">
<center>
EDI Integrator V9 - Copyright (c) 2013 /n software inc. - All rights reserved. - For more information, please visit our website at <a href="http://www.nsoftware.com/?demopg-BEJ9V" target="_blank">www.nsoftware.com</a>.</center></div>
</body></html>

