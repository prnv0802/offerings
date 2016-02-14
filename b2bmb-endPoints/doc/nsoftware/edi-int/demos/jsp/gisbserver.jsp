<html>
<head>
<title>EDI Integrator V9 Demos - GISB Server</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="EDI Integrator V9 Demos - GISB Server"></head>

<body>
<div id="content">
<h1>EDI Integrator - Demo Pages</h1>
<h2>GISB Server</h2>
<p>Shows how to use the GISBReceiver component.</p>
<a href="seecode.jsp?gisbserver.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="inedi.Gisbreceiver"%>

    <%

class gisbserver {
}

if (request.getMethod().equalsIgnoreCase("POST")) {

	try {
	String GPG_PATH = "c:\\Program Files (x86)\\GNU\\GnuPg\\gpg.exe";
	String HOMEDIR = "C:\\Program Files (x86)\\nsoftware\\EDI Integrator V9 Java Edition\\demos\\console\\gisbclient"; //directory of public and secret keyrings, trustdb
				
	//my private key info
	String PASSPHRASE = "testreceiver"; //passphrase of the secret key to be used to decrypt
	String USERID = null; // Key id of a key in my secret keyring to be used for decryption (optional, if not specified provider should be able to determine this)

	Gisbreceiver gisbreceiver1 = new Gisbreceiver();
	gisbreceiver1.readRequest(request);

	gisbreceiver1.setLogDirectory("c:\\gisb-logs");
	gisbreceiver1.setPGPProvider("gnupg_provider");
	gisbreceiver1.setPGPParam("gpg-path",GPG_PATH);
	gisbreceiver1.setPGPParam("homedir", HOMEDIR);
	gisbreceiver1.setPGPParam("userid",USERID);
	gisbreceiver1.setPGPParam("passphrase",PASSPHRASE);

	gisbreceiver1.parseFormData();
	String dataFrom = gisbreceiver1.getDataFrom();
	String dataTo = gisbreceiver1.getDataTo();

	if(dataFrom.compareTo("GISBTestSender") == 0 && dataTo.compareTo("GISBTestReceiver") == 0) {
		gisbreceiver1.parseRequest();
		gisbreceiver1.createResponse();
		gisbreceiver1.sendResponse(response);
		}
	else {
		response.sendError(400,"Unknown trading partner relationship, this server has been configured to receive from GISBSender and is identified by GISBReceiver");
	}


	
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

