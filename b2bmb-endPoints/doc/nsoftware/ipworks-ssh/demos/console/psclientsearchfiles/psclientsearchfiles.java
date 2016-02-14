/*
 * IP*Works! SSH V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import ipworksssh.IPWorksSSHException;
import ipworksssh.PSProperty;
import ipworksssh.Psclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class psclientsearchfiles extends ConsoleDemo {
  private static Psclient psclient1 = null;

  public static void main(String[] args) {
    try {
    	psclient1 = new Psclient();
    	psclient1.addPsclientEventListener(new ipworksssh.DefaultPsclientEventListener(){
    	
    	public void SSHServerAuthentication(ipworksssh.PsclientSSHServerAuthenticationEvent e)
    	{
    	  e.accept = true;
    	}
    	});
    	
    	psclient1.setSSHHost(prompt("Remote Host"));
    	psclient1.setSSHUser(prompt("User (DOMAIN\\Username)"));
    	psclient1.setSSHPassword(prompt("Password"));
    	
    	System.out.print("Search Path [C:\\]: ");
    	String searchPath = input();
    	if(searchPath.equals(""))
    		searchPath = "C:\\";
    	
    	System.out.print("Search Filter [*.txt]: ");
    	String searchFilter = input();
    	if(searchFilter.equals(""))
    		searchFilter = "*.txt";
    	
    	System.out.print("Search Term [test]: ");
    	String searchTerm = input();
    	if(searchTerm.equals(""))
    		searchTerm = "test";    
    	
    	char recurse = ask("Recurse");
    	
    	String command = "gci ";
    	
    	if(recurse == 'Y' || recurse == 'y')
    	{
    	  command += "-recurse -path \"" + searchPath + "\" -include " + searchFilter;	
    	}
    	else
    	{
    	  command += "-path \"" + searchPath + "\\" + searchFilter + "\"";
    	}
    	
    	command += " | select-string \"" + searchTerm + "\"";
    	
    	System.out.println("\n\nSearching ...\n\n");
    	
    	psclient1.SSHLogon(psclient1.getSSHHost(), 22);
    	psclient1.execute(command);
    	
    	String filePath = "";
    	String lineNum = "";
    	String lineText = "";
    	
    	System.out.format("%1$-40s %2$-8s %3$-80s\n","Path","Line Num","Line Text");
    	
    	if(psclient1.getPSObjectCount() == 0)
    	{
    		System.out.println("No matches found.");
    	}
    	else
    	{
    	
    	for(int i =0;i<psclient1.getPSObjectCount();i++)
    	{
    		filePath = "";
    		lineNum = "";
    		lineText = "";
    		
        	psclient1.setPSObjectIndex(i);
        	
        	for ( Iterator e = psclient1.getPSObject().values().iterator(); e.hasNext(); ) 
        	{
	               PSProperty prop = (PSProperty)e.next();
	               if(prop.getName().equals("LineNumber"))
	            	   lineNum = prop.getValue();
	               else if (prop.getName().equals("Line"))
	            	   lineText = prop.getValue();
	               else if (prop.getName().equals("Path"))
	            	   filePath = prop.getValue();
        	}
        	
        	System.out.format("%1$-40.40s %2$-8.8s %3$-80.80s\n",filePath,lineNum,lineText);
    	}
    	}
    	
    } catch (IPWorksSSHException e) {
    	displayError(e);
    } catch (Exception e) {
    	System.out.println(e.getMessage());
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
    
    if (e instanceof IPWorksSSHException) {
      System.out.print(" (" + ((IPWorksSSHException) e).getCode() + ")");    
    }    
    System.out.println(": " + e.getMessage());
    e.printStackTrace();
  }
}



