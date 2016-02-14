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

public class psclientgetservice extends ConsoleDemo {
	private static Psclient psclient1 = null;
	private static String user = "";
	private static String pass = "";
	private static String host = "";
	
	public psclientgetservice() {
		try {
			psclient1 = new Psclient(); 
	    	psclient1.addPsclientEventListener(new ipworksssh.DefaultPsclientEventListener(){
	        	
	        	public void SSHServerAuthentication(ipworksssh.PsclientSSHServerAuthenticationEvent e)
	        	{
	        	  e.accept = true;
	        	}
	        	});
	    	
	    	host = prompt("Remote Host");
	    	user = prompt("User (DOMAIN\\Username)");
	    	pass = prompt("Password");
	    	
	    	char actionCode;
	    	while ((actionCode = ask("Select an action", ".", "\n0) Quit\n1)List Services\n2)Start Service\n3)Stop Service\n4)Restart Service\n")) != '0') {
	    		if (actionCode == '1')
	    			ListServices();
	    		else if (actionCode == '2')
	    			StartService();
	    		else if (actionCode == '3')
	    			StopService();
	    		else if (actionCode == '4')
	    			RestartService();
	    		else 
	    			System.out.println("Unrecognized command");
	    	}
	    		        	
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	private static void RestartService() {
		String service = prompt("What service do you want to restart?");

    	try{
	    	psclient1.setSSHUser(user);
	    	psclient1.setSSHPassword(pass);
	    	psclient1.SSHLogon(host, 22);
	    	psclient1.execute("restart-service -name " + service);
	    	System.out.println(service + " restarted.");
    		psclient1.SSHLogoff();
    	} catch (IPWorksSSHException ex) {
    		displayError(ex);
    	} 
    	ListServices();
    			
	}
	
	private static void StartService() {
		String service = prompt("What service do you want to start?");

    	try{
	    	psclient1.setSSHUser(user);
	    	psclient1.setSSHPassword(pass);
	    	psclient1.SSHLogon(host, 22);
	    	psclient1.execute("start-service -name " + service);
	    	System.out.println(service + " started.");
    		psclient1.SSHLogoff();
    	} catch (IPWorksSSHException ex) {
    		displayError(ex);
    	} 
    	ListServices();
    			
	}
	
	private static void StopService() {
		String service = prompt("What service do you want to stop?");

    	try{
	    	psclient1.setSSHUser(user);
	    	psclient1.setSSHPassword(pass);
	    	psclient1.SSHLogon(host, 22);
	    	psclient1.execute("stop-service -name " + service);
	    	System.out.println(service + " stopped.");
    		psclient1.SSHLogoff();
    	} catch (IPWorksSSHException ex) {
    		displayError(ex);
    	} 
    	ListServices();
    			
	}
	
	private static void ListServices() {
		
		System.out.println("Checking for services ...\n");
    	
    	try{
	    	psclient1.setSSHUser(user);
	    	psclient1.setSSHPassword(pass);
	    	psclient1.SSHLogon(host, 22);
    		psclient1.execute("get-service");
    		
    		for (int i = 0; i < psclient1.getPSObjectCount(); i++) {
    			psclient1.setPSObjectIndex(i);
    			String name = "";
    			String displayName = "";
    			String status = "";
    			
    			for ( Iterator e = psclient1.getPSObject().values().iterator(); e.hasNext(); )
    			{
    				PSProperty prop = (PSProperty)e.next();
    				if (prop.getName().equals("ServiceName")) {
    					name = prop.getValue();
    				} else if (prop.getName().equals("DisplayName")) {
    					displayName = prop.getValue();
    				} else if (prop.getName().equals("Status")) {
    					if (prop.getValue().equals("1")) {
    						status = "Stopped";
    					} else if (prop.getValue().equals("4")) {
    						status = "Running";
    					} else {
    						status = prop.getValue();
    					}
    				}
    			}
    			
    			System.out.format("%1$-20s%2$-50s%3$-7s\n", name, displayName, status);
    		}
    		psclient1.SSHLogoff();

    	} catch (IPWorksSSHException ex) {
    		displayError(ex);
    	}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new psclientgetservice(); 
	}

}class ConsoleDemo {
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



