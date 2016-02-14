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

public class ediparser extends ConsoleDemo {
  public static void main(String[] args) {
    Edireader edireader1 = new Edireader();

    try {
    	
      System.out.println("****************************************************************");
      System.out.println("* This demo shows how to use the EDIReader component to parse  *");
      System.out.println("* an X12 document. A sample X12 document is provided as well.  *");
      System.out.println("****************************************************************");
    	
      edireader1.addEdireaderEventListener(new inedi.DefaultEdireaderEventListener(){
    	  public void endFunctionalGroup(EdireaderEndFunctionalGroupEvent e){
    		  System.out.println("EndFunctionalGroup: " + e.tag);
    	  }
    	  public void endInterchange(EdireaderEndInterchangeEvent e){
    		  System.out.println("EndInterchange: " + e.tag);
    	  }
    	  public void endLoop(EdireaderEndLoopEvent e){
    		  System.out.println("EndLoop");
    	  }
    	  public void endTransaction(EdireaderEndTransactionEvent e){
    		  System.out.println("EndTransaction: " + e.tag);
    	  }
    	  public void error(EdireaderErrorEvent e){
    		  System.out.println("ERROR: " + e.errorCode + ":" + e.description);
    	  }
    	  public void resolveSchema(EdireaderResolveSchemaEvent e){
    		  System.out.println("ResolveSchema: " + e.transactionCode);
    	  }
    	  public void segment(EdireaderSegmentEvent e){
    		  System.out.println("Segment: " + e.name);
    	  }
    	  public void startFunctionalGroup(EdireaderStartFunctionalGroupEvent e){
    		  System.out.println("StartFunctionalGroup: " + e.tag);
    	  }
    	  public void startInterchange(EdireaderStartInterchangeEvent e){
    		  System.out.println("StartInterchange: " + e.tag);
    	  }
    	  public void startLoop(EdireaderStartLoopEvent e){
    		  System.out.println("StartLoop: " + e.name);
    	  }
    	  public void startTransaction(EdireaderStartTransactionEvent e){
    		  System.out.println("StartTransaction: " + e.tag);
    	  }
    	  public void warning(EdireaderWarningEvent e){
    		  System.out.println("WARNING: " + e.warnCode + ": " + e.message);
    	  }
      });
      
      edireader1.config("Encoding=iso-8859-1");
      edireader1.setEDIStandard(edireader1.esX12);
      
      edireader1.loadSchema("./Compiled_X12_00401_810.bin");
     
     //This demo provides information about the parsed document through the events.
     //To navigate the document using the XPath property first set:
     //edireader1.config("ResolveXPathOnSet=true");
     
      edireader1.parseFile(prompt("EDI File To Parse",":","./X12.txt"));
      
    } catch (InEDIException ex) {
      System.out.println("InX12 exception thrown: " + ex.getCode() + " [" + ex.getMessage() + "].");
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
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



