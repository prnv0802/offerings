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

public class oftpclient {
	
	public oftpclient(){
		Oftpclient oftp = new Oftpclient();
		String buffer = "";
		try{
			oftp.addOftpclientEventListener(new OftpEvents(this));
			oftp.setOverwrite(true);
			
			System.out.println("Server: ");
			buffer = input();
			oftp.setRemoteHost(buffer);
			
			System.out.println("Server SSID: ");
			buffer = input();
			oftp.setServerSSIDCode(buffer);
			
			System.out.println("Server SFID: ");
			buffer = input();
			oftp.setServerSFIDCode(buffer);
			
			System.out.println("Server Password: ");
			buffer = input();
			oftp.setServerPassword(buffer);
			
			System.out.println("Client SSID: ");
			buffer = input();
			oftp.setClientSSIDCode(buffer);
			
			System.out.println("Client SFID: ");
			buffer = input();
			oftp.setClientSFIDCode(buffer);
			
			System.out.println("Client Password: ");
			buffer = input();
			oftp.setClientPassword(buffer);
			
			System.out.println("Would you like to [R]eceive or [S]end?: ");
			buffer = input();
			
			if(buffer.equals("r") || buffer.equals("R"))
			{
				System.out.println("Please enter the download directory: ");
				buffer = input();
				oftp.setDownloadDirectory(buffer);
				oftp.receiveFiles();
				System.out.println("done");
			}
			else
			{
				String localFile = "";
				System.out.println("Please enter the local file to upload: ");
				buffer = input();
				localFile = buffer;
				System.out.println("Please enter the virtual file name: ");
				buffer = input();
				oftp.sendFile(localFile,buffer);
				System.out.println("done");
			}
		}
		catch(InEDIException exc)
		{
			System.out.println("Exception: "+ exc.getMessage());
		}
		catch(Exception exc)
		{
			System.out.println("Exception: "+ exc.getMessage());
		}
	}
	
	public static void main(String[] args) {
		try{
			new oftpclient();
		}catch (Exception exc){
			System.out.println("Exception: "+ exc.getMessage());
		}
	}
	
    private String input() throws IOException
    {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        return bf.readLine();
    }
    
    public void endTransfer(OftpclientEndTransferEvent arg){
    	System.out.println("");
    }
    public void error(OftpclientErrorEvent arg){
    	System.out.print("ERROR! " + arg.description);
    }
    public void startTransfer(OftpclientStartTransferEvent arg){
    	System.out.print((arg.direction == 0 ? "Sending " : "Receiving ") + arg.virtualFileName);
    }
    public void transfer(OftpclientTransferEvent arg){
    	System.out.print(". ");
    }
}

class OftpEvents implements OftpclientEventListener{
		oftpclient instance;
	public OftpEvents(oftpclient instance)
	{
		this.instance = instance;
	}
	public void endTransfer(OftpclientEndTransferEvent arg){
		instance.endTransfer(arg);
	}
	
	public void error(OftpclientErrorEvent arg){
		instance.error(arg);
	}
	
	public void startTransfer(OftpclientStartTransferEvent arg){
		instance.startTransfer(arg);
	}
	public void transfer(OftpclientTransferEvent arg){
		instance.transfer(arg);
	}
	public void SSLStatus(OftpclientSSLStatusEvent arg){
	}	
	public void SSLServerAuthentication(OftpclientSSLServerAuthenticationEvent arg){
	}	
	public void endResponse(OftpclientEndResponseEvent arg){
	}
	public void certificateReceived(OftpclientCertificateReceivedEvent arg){
	}
	public void PITrail(OftpclientPITrailEvent arg)
	{
		//This event provides detailed logging of the interaction between the client and server
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



