/*
 * IP*Works! SSH V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import ipworksssh.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

public class sftpserver extends ConsoleDemo{
	public static class myFTPServer extends Sftpserver {
		/**
		 *
		 */
		public myFTPServer() {
			super();
			try {
				addSftpserverEventListener(new ipworksssh.SftpserverEventListener() {

					public void SSHStatus(SftpserverSSHStatusEvent e) {	
						Log(e.connectionId,e.message);
					}
					public void SSHUserAuthRequest(SftpserverSSHUserAuthRequestEvent e) {	
						if(e.user.equals("test") && !e.authMethod.equals("none") && e.authParam.equals("test"))
						{
							e.accept = true;
							Log(e.user + " has successfully authenticated.");
						}
					}
					public void connected(SftpserverConnectedEvent e) {	
						Log(e.connectionId,"Now Connected");
					}
					public void connectionRequest(SftpserverConnectionRequestEvent e) {
						Log(e.address + ":" + String.valueOf(e.port) + " is attempting to connect.");
					}
					public void dirCreate(SftpserverDirCreateEvent e) {	
						Log(e.user + " created the directory " + e.path);
					}
					public void dirList(SftpserverDirListEvent e) {	
					}
					public void dirRemove(SftpserverDirRemoveEvent e) {	
						Log(e.user + " deleted the directory " + e.path);
					}
					public void disconnected(SftpserverDisconnectedEvent e) {	
						Log(e.connectionId, "Now Disconnected");
					}
					public void error(SftpserverErrorEvent e) {	
						Log(e.connectionId, "Error: " + e.description);
					}
					public void fileClose(SftpserverFileCloseEvent e) {	
						Log(e.user + " transferred " + e.path);
					}
					public void fileOpen(SftpserverFileOpenEvent e) {	
						String operation = "";
						if((e.flags & 1) != 0) //Read
							operation = "downloading";
						if((e.flags % 2) != 0)
							operation = "uploading";
						
						Log(e.user + " started " + operation + " " + e.path);
					}
					public void fileRead(SftpserverFileReadEvent e) {	
					}
					public void fileRemove(SftpserverFileRemoveEvent e) {	
						Log(e.user + " deleted the file " + e.path);
					}
					public void fileRename(SftpserverFileRenameEvent e) {
						Log(e.user + " renamed the file " + e.path);
					}
					public void fileWrite(SftpserverFileWriteEvent e) {	
					}
					public void getAttributes(SftpserverGetAttributesEvent e) {	
					}
					public void resolvePath(SftpserverResolvePathEvent e) {	
					}
					public void setAttributes(SftpserverSetAttributesEvent e) {
					}
					
				});
				
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void Log(String message)
	{
		System.out.println(message);
	}
	
	private static void Log(String connectionId, String message)
	{
		Log("[" + connectionId + "] " + message);
	}
	
	private static myFTPServer sftpserver1 = null;

	public static void main(String[] args) {
		sftpserver1 = new myFTPServer();

		try {
			System.out.println("**********************************************************");
			System.out.println("* This demo shows how to use the SFTPServer component to *");
			System.out.println("* create a simple SFTP Server.                           *");
			System.out.println("* Use the following credentials to connect.              *");
			System.out.println("* User: test                                             *");
			System.out.println("* Password: test                                         *");
			System.out.println("**********************************************************");
			
			//For the purposes of this demo we are using the included certificate. You may change this line to specify your own certificate.
			sftpserver1.setSSHCert(new ipworksssh.Certificate(Certificate.cstPFXFile, "sftpserver.pfx", "demo", "*"));
			
			sftpserver1.setRootDirectory(prompt("Root Directory", ":", "./"));
			sftpserver1.setLocalPort(Integer.parseInt(prompt("Port", ":", "22")));
			sftpserver1.setListening(true);
			
			System.out.println("Server listening on port " + sftpserver1.getLocalPort() + ".");
			System.out.println("Press Q to exit.\r\n\r\n");
			
			while(true)
			{
				if(System.in.available()>0)
				{
					if(String.valueOf(read()).equalsIgnoreCase("Q"))
					{
						System.out.println("Server shutting down. Goodbye!");
						sftpserver1.shutdown();
					}
				}
			}
			
		} catch (IPWorksSSHException e) {
			displayError(e);
		} catch (Exception e) {
			displayError(e);
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



