/*
 * IP*Works! SSH V9 Java Edition - Demo Application
 *
 * Copyright (c) 2013 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import ipworksssh.IPWorksSSHException;
import ipworksssh.Sftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

public class sftp extends ConsoleDemo{
	public static class myFTP extends Sftp {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		public long transtime;
		public long transbytes;

		public myFTP() {
			super();
			try {
				addSftpEventListener(new ipworksssh.SftpEventListener() {
					public void dirList(ipworksssh.SftpDirListEvent e) {
						System.out.println(e.dirEntry);
					}
					public void connected(ipworksssh.SftpConnectedEvent e) {
					}
					public void connectionStatus(ipworksssh.SftpConnectionStatusEvent e) {
					}
					public void disconnected(ipworksssh.SftpDisconnectedEvent e) {
					}
					public void endTransfer(ipworksssh.SftpEndTransferEvent e) {
						long endtime;
						endtime = System.currentTimeMillis();
						transtime = endtime - sftp1.transtime;
					}
					public void error(ipworksssh.SftpErrorEvent e) {
						System.out.println("Error "+e.errorCode+": "+e.description);
					}
					
					public void SSHCustomAuth(ipworksssh.SftpSSHCustomAuthEvent e){}
					
					public void SSHKeyboardInteractive(ipworksssh.SftpSSHKeyboardInteractiveEvent e) {
					}					
					public void SSHServerAuthentication(ipworksssh.SftpSSHServerAuthenticationEvent e) {
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
					public void SSHStatus(ipworksssh.SftpSSHStatusEvent e) {
					}
					public void startTransfer(ipworksssh.SftpStartTransferEvent e) {
						transtime = System.currentTimeMillis();
					}
					public void transfer(ipworksssh.SftpTransferEvent e) {
						transbytes = e.bytesTransferred;
					}
				});
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
		}
	}
	private static void logon(myFTP ftp) throws IPWorksSSHException {
		String host = prompt("Host", ":");
			int port = Integer.valueOf(prompt("Port", ":", "22")).intValue();
			ftp.setSSHUser(prompt("User", ":"));
			ftp.setSSHPassword(prompt("Password", ":"));

			ftp.SSHLogon(host,port);
	}
	private static myFTP sftp1 = null;

	public static void main(String[] args) {
		sftp1 = new myFTP();
		String command;
		String[] argument;
		String pathname;

		try {
			logon(sftp1);
			
			System.out.println("?        bye     help     put     rmdir");
			System.out.println("append   cd      ls       pwd     mkdir");
			System.out.println("quit     get     open     rm");
			
			while (true) {
				sftp1.setRemoteFile("");
				command = prompt("sftp", ">");
				argument = command.split("\\s");	// argument[0] is a command name
				if (argument.length == 0) {
					;	// do nothing
				} else	if (argument[0].equalsIgnoreCase("?") || argument[0].equalsIgnoreCase("help")) {
					System.out.println("?        bye     help     put     rmdir");
					System.out.println("append   cd      ls       pwd     mkdir");
					System.out.println("quit     get     open     rm");
				} else if (argument[0].equalsIgnoreCase("append")) {	// append localfile remotefile
					if(argument.length <3)
					{
						System.out.println("usage: append localFile remoteFile");
						continue;
					}
					sftp1.setLocalFile(argument[1]);
					sftp1.setRemoteFile(argument[2]);
					sftp1.append();
				} else if (argument[0].equalsIgnoreCase("bye") || argument[0].equalsIgnoreCase("quit")) {
					sftp1.SSHLogoff();
					System.out.println("Goodbye.");
					System.exit(0);
				} else if (argument[0].equalsIgnoreCase("cd")) {	// cd remotepath
					if(argument.length <2)
					{
						System.out.println("usage: cd remotePath");
						continue;
					}
					sftp1.setRemotePath(argument[1]);
				} else if (argument[0].equalsIgnoreCase("get")) {	// get remotefile
					if(argument.length <2)
					{
						System.out.println("usage: get remoteFile");
						continue;
					}					
					sftp1.setRemoteFile(argument[1]);
					sftp1.setLocalFile(argument[1]);
					sftp1.download();
					System.out.println(sftp1.transbytes
							+" bytes sent in "+((float)sftp1.transtime/1000.)
							+" seconds. ("+((float)sftp1.transbytes)/((float)sftp1.transtime)
							+" KBps)");
				} else if (argument[0].equalsIgnoreCase("ls")) {	// ls [dir]
					if (argument.length > 1) {
						pathname = sftp1.getRemotePath();
						sftp1.setRemotePath(argument[1]);
						sftp1.listDirectory();
						sftp1.setRemotePath(pathname);
					} else {
						sftp1.listDirectory();
					}
				} else if (argument[0].equalsIgnoreCase("mkdir")) {	// mkdir dir
					if(argument.length <2)
					{
						System.out.println("usage: mkidr directory");
						continue;
					}
					sftp1.makeDirectory(argument[1]);
				} else if (argument[0].equalsIgnoreCase("mv")) {	// mv remotefile1 remotefile2
					if(argument.length <3)
					{
						System.out.println("usage: mv remoteFile newRemoteFile");
						continue;
					}					
					sftp1.setRemoteFile(argument[1]);
					sftp1.renameFile(argument[2]);
				} else if (argument[0].equalsIgnoreCase("open")) {	// open				
					sftp1.SSHLogoff();
					logon(sftp1);
				} else if (argument[0].equalsIgnoreCase("put")) {	// put localfile
					if(argument.length <2)
					{
						System.out.println("usage: put localFile");
						continue;
					}						
					java.io.File localFile = new java.io.File(argument[1]);
					sftp1.setRemoteFile(localFile.getName());
					sftp1.setLocalFile(argument[1]);
					sftp1.upload();
					System.out.println(sftp1.transbytes
							+" bytes sent in "+((float)sftp1.transtime/1000.)
							+" seconds. ("+((float)sftp1.transbytes)/((float)sftp1.transtime)
							+" KBps)");
				} else if (argument[0].equalsIgnoreCase("pwd")) {
					System.out.println(sftp1.getRemotePath());
				} else if (argument[0].equalsIgnoreCase("rm")) {	// rm file
					if(argument.length <2)
					{
						System.out.println("usage: rmdir file");
						continue;
					}	
					sftp1.deleteFile(argument[1]);
				} else if (argument[0].equalsIgnoreCase("rmdir")) {	// rmdir dir
					if(argument.length <2)
					{
						System.out.println("usage: rmdir directory");
						continue;
					}	
					sftp1.removeDirectory(argument[1]);
				} else if (argument[0].length() == 0) {
					;	// Do nothing
				} else {
					System.out.println("Bad command / Not implemented in demo.");
				}
			}
		} catch (IPWorksSSHException e) {
			System.out.println("IPWorksSSHException: code="+e.getCode()+"; description=\""+e.getMessage()+"\"");
		} catch (Exception e) {
                  	System.out.println("Exception: message=\""+e.getMessage()+"\"");
			e.printStackTrace();
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



