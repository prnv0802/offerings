/*
 * EDI Integrator V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import inedi.GISBData;
import inedi.Gisbsender;
import inedi.InEDIException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class gisbclient {

	public static void main(String[] args) {
		Gisbsender gisbsender1 = new Gisbsender();
		String gbgPath = "c:\\Program Files (x86)\\GNU\\GnuPg\\gpg.exe";
		String homedir = "C:\\Program Files (x86)\\nsoftware\\EDI Integrator V9 Java Edition\\demos\\console\\gisbclient";
		String passphrase = "testsender";
		String userid = "GISBSender";
		String receiverid = "GISBReceiver";
		
		try{
		gisbsender1.setDataFrom("GISBTestSender");
		gisbsender1.setDataTo("GISBTestReceiver");
		gisbsender1.setURL("http://localhost/inedi/gisbserver.jsp");
		
		GISBData data = gisbsender1.getGISBData();
		data.setEDIType("X12");
		data.setData("Paste EDI data here.");
		gisbsender1.setGISBData(data);
		
		// If you set a log directory, the component will produce detailed log files.
		gisbsender1.setLogDirectory("/gisb-logs");			
	
		gisbsender1.setPGPProvider("gnupg_provider");
		gisbsender1.setPGPParam("gpg-path",gbgPath);
		gisbsender1.setPGPParam("homedir", homedir);
		
		//set properties for signing
		gisbsender1.setPGPParam("passphrase",passphrase);
		gisbsender1.setPGPParam("userid",userid);
		gisbsender1.setSignData(true);

		//set properties for encryption
		gisbsender1.setPGPParam("recipient-userid",receiverid);
		gisbsender1.setEncryptData(true);
			
		gisbsender1.post();
					
		// If the call to post returns without throwing an exception, then the
		// component was able to post the data and get a response. 
		System.out.println("Transmission was successful.");
		}
		catch(InEDIException exc)
		{
			System.out.println("Transmission was not successful: " + exc.getMessage());
		}
		finally 
		{
		System.out.print(new String(gisbsender1.getResponseContent()));	
			
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



