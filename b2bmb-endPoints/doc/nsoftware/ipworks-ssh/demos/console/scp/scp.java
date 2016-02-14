/*
 * IP*Works! SSH V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import ipworksssh.IPWorksSSHException;
import ipworksssh.Scp;
import ipworksssh.Sexec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

public class scp extends ConsoleDemo {
	public static class mySCP extends Scp {
		/**
		 *
		 */

		public mySCP() {
			super();
			try {
				addScpEventListener(new ipworksssh.ScpEventListener() {
					public void connected(ipworksssh.ScpConnectedEvent e) {}
					
					public void connectionStatus(ipworksssh.ScpConnectionStatusEvent e) {}
					
					public void disconnected(ipworksssh.ScpDisconnectedEvent e) {}
					
					public void endTransfer(ipworksssh.ScpEndTransferEvent e) {
						if (e.direction == 0) //client
						{
							System.out.println("Uploaded " + e.localFile + " to " + e.remoteFile);
						}
						else //server
						{
							System.out.println("Downloaded " + e.localFile + " from " + e.remoteFile);
						}
					}
					
					public void error(ipworksssh.ScpErrorEvent e) {}
					
					public void SSHCustomAuth(ipworksssh.ScpSSHCustomAuthEvent e){}
					
					public void SSHKeyboardInteractive(ipworksssh.ScpSSHKeyboardInteractiveEvent e) {}			
					
					public void SSHServerAuthentication(ipworksssh.ScpSSHServerAuthenticationEvent e) {
						if (e.accept) {
							return;
						}
						System.out.println("Server provided the certificate which has following fingerprint: " + e.fingerprint);
						char answer = ask("Would you like to continue anyways");
						if (answer == 'y') {
							e.accept = true;
						} else {
							System.out.println("exited.");
							System.exit(0);
						}
						return; 
					}
					public void SSHStatus(ipworksssh.ScpSSHStatusEvent e) {
						System.out.println(e.message);
					}
					
					public void startTransfer(ipworksssh.ScpStartTransferEvent e) {
						if (e.direction == 0) //client
						{
							System.out.println("Uploading " + e.localFile + " to " + e.remoteFile);
						}
						else //server
						{
							System.out.println("Downloading " + e.localFile + " from " + e.remoteFile);
						}
					}
					
					public void transfer(ipworksssh.ScpTransferEvent e) {
						if (e.direction == 0) //client
						{
							System.out.println(e.percentDone + "% Uploaded");
						}
						else //server
						{
							System.out.println(e.percentDone + "% Downloaded");
						}
					}
				});
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class mySExec extends Sexec {
		private static final long serialVersionUID = 1L;
		public boolean stdoutFired = false;
		
		public mySExec() {
			super();
			try {
				addSexecEventListener(new ipworksssh.SexecEventListener() {

					@Override
					public void SSHCustomAuth(ipworksssh.SexecSSHCustomAuthEvent e) {}

					@Override
					public void SSHKeyboardInteractive(ipworksssh.SexecSSHKeyboardInteractiveEvent e) {}

					@Override
					public void SSHServerAuthentication(ipworksssh.SexecSSHServerAuthenticationEvent e) {
						//If this is reached then the server certificate has already been accepted
						e.accept = true;
					}

					@Override
					public void SSHStatus(ipworksssh.SexecSSHStatusEvent e) {}

					@Override
					public void connected(ipworksssh.SexecConnectedEvent e) {}

					@Override
					public void connectionStatus(ipworksssh.SexecConnectionStatusEvent e) {}

					@Override
					public void disconnected(ipworksssh.SexecDisconnectedEvent e) {}

					@Override
					public void error(ipworksssh.SexecErrorEvent e) {}

					@Override
					public void stderr(ipworksssh.SexecStderrEvent e) {
						System.out.println("Error " + new String(e.text));
					}

					@Override
					public void stdout(ipworksssh.SexecStdoutEvent e) {
						String[] out = new String(e.text).split("\n");
						for (int i = 0; i < out.length; i++) {
							System.out.println(out[i]);
						}
						stdoutFired = true;
					}
				});
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
		}
	}

	private static mySCP scp1 = null;
	private static mySExec sexec1 = null;

	public static void main(String[] args) {
		scp1 = new mySCP();
		sexec1 = new mySExec();
		String command;
		String[] argument;
		
		System.out.println("This demo shows how to use the SCP component to securely copy files to and from a ");
		System.out.println("remote server. The SExec component is used here to list the specified remote directory.");
		System.out.println();

		try {
			String host = prompt("Host", ":");		
			int port = Integer.valueOf(prompt("Port", ":", "22")).intValue();
		
			String user = prompt("User", ":");
			scp1.setSSHUser(user);
			sexec1.setSSHUser(user);

			String pass = prompt("Password", ":");
			scp1.setSSHPassword(pass);
			sexec1.setSSHPassword(pass);

			scp1.SSHLogon(host,port);
			sexec1.SSHLogon(host,port);
			
			System.out.println();
			System.out.println("The valid commands for this demo are as follows:");
			System.out.println("?     exit     help     ls [dir]     put     get");
			System.out.println();
			
			while (true) {
				scp1.setRemoteFile("");
				command = prompt("scp", ">");
				argument = command.split("\\s");	// argument[0] is a command name
				if (argument.length == 0) {
					;	// do nothing
				} else	if (argument[0].equalsIgnoreCase("?") || argument[0].equalsIgnoreCase("help")) {
					System.out.println("?     exit     help     ls [dir]     put     get");
				} else if (argument[0].equalsIgnoreCase("exit")) {
					scp1.SSHLogoff();
					System.out.println("Goodbye.");
					System.exit(0);
				} else if (argument[0].equalsIgnoreCase("ls")) {	// ls [dir]
					if (argument.length == 2) {
						sexec1.execute("ls -p " + argument[1]);
					} else {
						sexec1.execute("ls -p ");
					}
					
					while (!sexec1.stdoutFired) {
						sexec1.doEvents();
					}
					sexec1.stdoutFired = false;
					
				} else if (argument[0].equalsIgnoreCase("put")) {	// put localfile remoteFile
					if(argument.length != 3)
					{
						System.out.println("usage: put localFile remoteFile");
						continue;
					}						
					scp1.setRemoteFile(argument[2]);
					scp1.setLocalFile(argument[1]);
					scp1.upload();
				} else if (argument[0].equalsIgnoreCase("get")) {	// get remotefile localFile

					if(argument.length != 3)
					{
						System.out.println("usage: get remoteFile localFile");
						continue;
					}					
					scp1.setRemoteFile(argument[1]);
					scp1.setLocalFile(argument[2]);
					scp1.download();
				} else if (argument[0].length() == 0) {
					;	// Do nothing
				} else {
					System.out.println("Bad command / Not implemented in demo.");
				}
				
				scp1.doEvents();
				sexec1.doEvents();
			}
		} catch (Exception e) {
			displayError(e);
		}
        System.out.println("exited.");
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



