<html>
<head>
<title>EDI Integrator V9 Demos - Async MDN Receiver</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="EDI Integrator V9 Demos - Async MDN Receiver"></head>

<body>
<div id="content">
<h1>EDI Integrator - Demo Pages</h1>
<h2>Async MDN Receiver</h2>
<p>Sample code illustrating how to receive an asynchronous MDN.</p>
<a href="seecode.jsp?asyncmdn.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="inedi.As2sender"%>
    <%@ page import="inedi.Certificate" %>

    <%

if (request.getMethod().equalsIgnoreCase("POST")) {
	try {

		// Note that we are using the As2sender control to receive asynchronous MDNs. This is to
		// allow the same API used to verify synchronous MDNs to be used for asynchronous MDNs as well.

		As2sender as2 = new As2sender();

		as2.setLogDirectory("/inedi-async-mdn-logs");

		// First, we need to read the receipt. This will allow to determine who the receipt is from,
		// which message it is in response to, and so on.

		as2.readAsyncReceipt(request); 
			
		// You should now check the AS2From and AS2To properties. These will correspond to the
		// originator and recipient of the *original message*; i.e., the sender of the receipt
		// will be stored in AS2To.

		// You should set your trading partner's certificate (unless you requested an unsigned
		// receipt). In a real server, this would be selected based on the value of AS2To.

		as2.setReceiptSignerCert(new Certificate("as2receiver.cer"));

		// readAsyncReceipt will also set the value of MessageId. You should now read this
		// value, and look up the values of OriginalContentMIC and MDNOptions, which you need to
		// have stored when you sent the original message. You have two options here: you 
		// can either store this information yourself, or if you have set the AsyncMDNInfoDir
		// when you sent the original message, you can set the same value to read this info
		// from the disk.

		// This value will be typical (although others are possible)
		as2.setMDNOptions("signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1, md5");
		// The OriginalContentMIC value can be read immediately after a successful post.
		as2.setOriginalContentMIC("abcdefghijklmnopqrstuvwzyz0=, sha1");

		// Finally, you are ready to verify the receipt. The call to verifyReceipt will
		// throw an exception if the bean is unable to verify the receipt.

		as2.verifyReceipt();

		response.setStatus(200);
	}
	catch (Exception ex) {
		ex.printStackTrace();
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

