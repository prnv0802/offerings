/*
 * IP*Works! SSH V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import ipworksssh.IPWorksSSHException;
import ipworksssh.Sexec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class sexec extends ConsoleDemo{

	private static class mySexec extends Sexec {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public mySexec() {
			super();
			try {
				addSexecEventListener(new ipworksssh.SexecEventListener() {
					public void stdout(ipworksssh.SexecStdoutEvent e) {
						System.out.print(new String(e.text));
					}
					public void connected(ipworksssh.SexecConnectedEvent e) {
					}
					public void connectionStatus(ipworksssh.SexecConnectionStatusEvent e) {
					}
					public void disconnected(ipworksssh.SexecDisconnectedEvent e) {
					}
					public void error(ipworksssh.SexecErrorEvent e) {
					}
					public void SSHCustomAuth(ipworksssh.SexecSSHCustomAuthEvent e){}
					public void SSHKeyboardInteractive(ipworksssh.SexecSSHKeyboardInteractiveEvent e) {
					}					
					public void SSHServerAuthentication(ipworksssh.SexecSSHServerAuthenticationEvent e) {
						e.accept = true;
					}
					public void SSHStatus(ipworksssh.SexecSSHStatusEvent e) {
					}
					public void stderr(ipworksssh.SexecStderrEvent e) {
					}
				});
			} catch (java.util.TooManyListenersException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		mySexec sexec1 = new mySexec();
		String host = "";
		int port = 22;
		String user = "";
		String password = "";

		try {
			host = prompt("Host", ":");
			port = Integer.valueOf(prompt("Port number", ":", "22")).intValue();
			user = prompt("User", ":");
			password = prompt("Password", ":");
			
      System.out.println("Connecting to "+host+"; port="+port);
			sexec1.setTimeout(60);
			sexec1.setSSHUser(user);
			sexec1.setSSHPassword(password);
			sexec1.SSHLogon(host, port);

			String commandEscape = "Q";
			System.out.println("******************************************************");
			System.out.println("Entering the Sexec command loop.");
			System.out.println("Type \""+commandEscape+"\" to exit.");
			System.out.println("******************************************************");
			// command loop
			for (String s = prompt("Command", ":"); !s.equalsIgnoreCase(commandEscape); s=prompt("Command", ":")) {
				sexec1.execute(s);
			}
			System.out.println("exited.");
			if (sexec1.isConnected()) {
				sexec1.SSHLogoff();
			}
		} catch (IPWorksSSHException ipwe) {
                	System.out.println("code="+ipwe.getCode()+"; message=\""+ipwe.getMessage()+"\"");
			ipwe.printStackTrace();
		}
		System.exit(0);
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



