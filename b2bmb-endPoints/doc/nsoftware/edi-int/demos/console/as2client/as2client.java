/*
 * EDI Integrator V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import inedi.As2sender;
import inedi.Certificate;
import inedi.EDIData;
import inedi.InEDIException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// This class illustrates how to write a simple AS2 client using the EDI Integrator package.
// (See the sample JSP pages for examples of server-side code.)

public class as2client {
	
	public static void main(String[] args) {
		try {
			new as2client().runTest();
		}
		catch (InEDIException ipwedie) {
			ipwedie.printStackTrace();
		}
	}
	
	public void runTest() throws InEDIException {
		As2sender as2sender = new As2sender();
		
		// The first thing to do is to specify the necessary AS2 identifiers.
			
		as2sender.setAS2From("AS2 Test Sending Organization");
		as2sender.setAS2To("AS2 Test Receiving Organization");
		
		// At this point, you should set the certificates for yourself and your trading partner.
		// For yourself, you will need a certificate with a private key, in PKCS#12 (PFX) or JKS format.
		// If you don't know the subject for your certificate, you can use the CertMgr bean to determine it.
		
		// Note that setting a signing certificate will instruct the bean to sign the message. Leave these
		// properties blank to send an unsigned message.
		
		// this will open the store, so set the type and password first!

		as2sender.setSigningCert(new Certificate(Certificate.cstPFXFile, "as2sender.pfx",
				"test", "CN=AS2 Test Sending Organization"));
	  
		// Similarly, setting the recipient's certificate will instruct the bean to encrypt the message.
		// If you want to set a certificate, but don't want to encrypt the message, you can set
		// EncryptionAlgorithm to an empty string.

		as2sender.getRecipientCerts().add(new Certificate("as2receiver.cer"));
				
		// There is also a ReceiptSignerCert property, which you should set if your trading partner
		// uses different certs for signing and encryption.
		
		// To request an MDN (Message Disposition Notification) based receipt, you should set the MDNTo
		// property. By default the bean will request a SIGNED receipt, with a Received-Content-MIC
		// value that establishes digital non-repudiation. If you prefer to receive an unsigned receipt
		// you should set MDNOptions to an empty string.
		
		as2sender.setMDNTo("as2@nsoftware.com"); // Note: the actual value is irrelevant, most servers just check to see if
																			 // something is specified at all.
		
		// By default, the bean will request that the receipt be delivered synchronously over the same
		// HTTP connection. If you prefer to receive your receipt asynchronously, you should set
		// MDNDeliveryOption, and provide additional processing for inbound asynchronous receipts.
		// (See the Async MDN JSP demo.)
		//as2sender.setMDNDeliveryOption("http://localhost:8080/as2-new/AsyncMDN");		
		
		// If you set a log directory, the bean will produce detailed log files.
		
		as2sender.setLogDirectory("/inedi-logs");
		
		// Set your partner's URL (HTTP or HTTPS) and the data to be sent. Note that if you are posting
		// to an HTTPS URL, you will likely need to set SSLAcceptServerCert.
		
		
		as2sender.setURL("http://your_partners_url/");
		as2sender.setEDIData(new EDIData("ISA*FOO***BAR***ETC., ETC., ETC.".getBytes(), "application/edi-x12")); // Alternatively, set EDIFile.
		
		// Now, you're ready to post:
		try 
		{
			// If the call to post() returns without throwing an exception, then the bean was able to post
			// the data and verify the response. In particular, if you requested a synchronous MDN,
			// it will automatically be validated, and an exception will be thrown if there are any problems.
			
			// If you requested an asynchronous MDN, you will need to save the values of MessageId,
			// OriginalContentMIC, and MDNOptions, so they can be looked up based on the MessageId.
			// Then, when you process the asynchronous MDN, you will need to load these values into
			// the bean to verify the MDN. See the Async MDN demo or the sample application for more
			// details on how to do this.
			as2sender.post();
		} 
		catch (InEDIException ex)
		{
			System.out.println(ex.getMessage());
		}
		finally 
		{
			System.out.println(new String(as2sender.getMDNReceipt().getContent()));
		}
	}
	
}
class ConsoleDemo {
  private static BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

  static String input() {
    try {
      return bf.readLine();
    } catch (IOException ioe) {
      return "";
    }
  }
  static char read() {
    return input().charAt(0);
  }

  static String prompt(String label) {
    return prompt(label, ":");
  }
  static String prompt(String label, String punctuation) {
    System.out.print(label + punctuation + " ");
    return input();
  }

  static String prompt(String label, String punctuation, String defaultVal)
  {
	System.out.print(label + " [" + defaultVal + "] " + punctuation + " ");
	String response = input();
	if(response.equals(""))
		return defaultVal;
	else
		return response;
  }

  static char ask(String label) {
    return ask(label, "?");
  }
  static char ask(String label, String punctuation) {
    return ask(label, punctuation, "(y/n)");
  }
  static char ask(String label, String punctuation, String answers) {
    System.out.print(label + punctuation + " " + answers + " ");
    return Character.toLowerCase(read());
  }

  static void displayError(Exception e) {
    System.out.print("Error");
    
    if (e instanceof InEDIException) {
      System.out.print(" (" + ((InEDIException) e).getCode() + ")");    
    }    
    System.out.println(": " + e.getMessage());
    e.printStackTrace();
  }
}



