/*
 * EDI Integrator V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import inedi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class certmgr {
    Certmgr certmgr1;
    int i = 0;
    public certmgr(){
        try
        {
            final String defaultCertStoreFile = "myidentities.jks";
            final String defaultCertStorePassword = "password";
            String filename = "";
            certmgr1 = new Certmgr();
            certmgr1.addCertmgrEventListener(new CertmgrEvents(this));
            String buffer; //user input
            // read a cert store file
            System.out.print("Please enter java key store (.jks) path [\""+defaultCertStoreFile+"\"]: ");
            buffer = input();
            filename = (buffer.length()>0)?buffer:defaultCertStoreFile;
            certmgr1.setCertStoreType(certmgr1.cstJKSFile); //user java key store (JKS file)
            certmgr1.setCertStore(filename);

            System.out.print("Please enter store password [\""+defaultCertStorePassword+"\"]: ");
            buffer = input();
            // This demo doesn't allow an empty string as a password for the .jks file.
            // If it is an empty string, a string "password" will be used.
            if (buffer.length() > 0)
            {
              certmgr1.setCertStorePassword(buffer);
            }
            else
            {
              certmgr1.setCertStorePassword(defaultCertStorePassword);
            }
            certmgr1.listStoreCertificates();
        }catch(InEDIException ex){
            System.out.println("IPWorks exception thrown: " + ex.getCode() + " [" + ex.getMessage() + "].");
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        System.out.println("Exited.");
    }
    public static void main(String[] args) {
	new certmgr();
    }
    private String input() throws IOException
    {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        return bf.readLine();
    }
    public void certList(CertmgrCertListEvent args) {
        i++;
        System.out.println(i + ". " + args.certSubject);
    }
}
class CertmgrEvents implements CertmgrEventListener{
		certmgr instance;
    public CertmgrEvents(certmgr instance){
        this.instance = instance;
    }
    public void certChain(CertmgrCertChainEvent args) {
    }
    public void certList(CertmgrCertListEvent args) {
        instance.certList(args);
    }
    public void error(CertmgrErrorEvent args) {
    }
    public void keyList(CertmgrKeyListEvent args) {
    }
    public void storeList(CertmgrStoreListEvent args) {
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



