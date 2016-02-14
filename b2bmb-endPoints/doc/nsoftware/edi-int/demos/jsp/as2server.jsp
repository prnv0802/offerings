<html>
<head>
<title>EDI Integrator V9 Demos - AS2 Server</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="EDI Integrator V9 Demos - AS2 Server"></head>

<body>
<div id="content">
<h1>EDI Integrator - Demo Pages</h1>
<h2>AS2 Server</h2>
<p>A simple example of an AS2 server.</p>
<a href="seecode.jsp?as2server.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="inedi.As2receiver"%>
    <%@ page import="inedi.Certificate" %>
    <%@ page import="inedi.InEDIException" %>

    <%

if (request.getMethod().equalsIgnoreCase("POST")) {
	As2receiver as2 = new As2receiver();

	try {
		as2.setLogDirectory("/inedi-server-logs");
		
		// The first order of business is to read the incoming request. This will
		// make sure it is an AS2 message, and will also determine the values of
		// AS2From and AS2To. 
		as2.readRequest(request);
				
				
		// In a real AS2 application you would now verify the values of AS2From and
		// AS2To, and look up your trading partner's certificate in a database.
		// This demo application simply processes data from anyone.

		// To sign receipts and/or decrypt incoming transmissions, you will need to
		// set your certificate. (Note that by default, you do not need to explicitly
		// tell the bean to sign or decrypt; per AS2 standards message security is
		// at the option of the client.
		
		// For yourself, you will need a certificate with a private key, in PKCS#12 (PFX) 
		// or JKS format. If you don't know the subject for your certificate, you can use the 
		// CertMgr bean to determine it.
				
		as2.setCertificate(new Certificate(Certificate.cstPFXFile, application.getRealPath("as2receiver.pfx"), "test", "*"));
				 
		// To verify signatures on inbound messages you will need to set your trading partner's
		// certificate (in PKCS7 or Base-64 encoded format). The special value "*" will instruct
		// the bean to accept any certificate included in a signature. This value is NOT
		// recommended for production.
 				
		as2.setSignerCert(new Certificate(application.getRealPath("as2sender.cer")));

		// Now, we are ready to process the request. This will parse the incoming data, verify
		// the signature and decrypt if necessary, and prepare an MDN receipt if requested.
		// In case finer control is needed, you may first call parseRequest and then
		// createMDNReceipt. Please see the documentation for more details.   
								
		boolean errorOccurred = false;
		try {
			as2.processRequest();

			// At this point a number of properties will be populated including EDIData and
			// EDIType, SignatureType, EncryptionType, CompressionFormat, etc. At a minimum
			// you will want to save EDIData to a file for later processing. You may also
			// wish to inspect other properties such as SignatureType and EncryptionType
			// to make sure the message security was what you were expecting.

		}
		catch (InEDIException ex) {
					
			// If an error occurs, EDIData and EDIType will not be populated, but the bean will
			// still create an MDN receipt reporting the error. If you want to send this receipt
			// to the client, you should catch this exception.
				
			// You may also want to log the error (although this will be done in the LogDirectory
			// specified above.)
					
			errorOccurred = true;
		}
				
		// This will send the response -- synchronously or asynchronously as requested by the client.

		as2.sendResponse(response);

	}
	catch (Exception ex) {
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

