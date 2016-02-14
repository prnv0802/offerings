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

public class psclientgetprocess extends ConsoleDemo {
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
    	
    	System.out.println("\n\nObtaining Results ...\n\n");
    	
    	psclient1.SSHLogon(psclient1.getSSHHost(), 22);
    	psclient1.execute("get-process");
    	
    	String processName = "";
    	String processId = "";
    	String handleCount = "";
    	String virtualMemSize = "";
    	
    	System.out.format("%1$-20s%2$-8s%3$-10s%4$-10s\n\n","Process","Id","Handles","Memory");
    	
    	for(int i =0;i<psclient1.getPSObjectCount();i++)
    	{
        	processName = "";
        	processId = "";
        	handleCount = "";
        	virtualMemSize = "";
    		
        	psclient1.setPSObjectIndex(i);
        	
        	for ( Iterator e = psclient1.getPSObject().values().iterator(); e.hasNext(); ) 
        	{
	               PSProperty prop = (PSProperty)e.next();
	               if(prop.getName().equals("ProcessName"))
	            	   processName = prop.getValue();
	               else if (prop.getName().equals("Id"))
	            	   processId = prop.getValue();
	               else if (prop.getName().equals("HandleCount"))
	            	   handleCount = prop.getValue();
	               else if (prop.getName().equals("VirtualMemorySize"))
	            	   virtualMemSize = prop.getValue();	               
        	}
        	
        	System.out.format("%1$-20s%2$-8s%3$-10s%4$-10s\n", processName, processId, handleCount, virtualMemSize);
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



