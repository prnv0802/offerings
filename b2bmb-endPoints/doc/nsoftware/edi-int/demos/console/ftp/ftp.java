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

public class ftp
{
    Ftps ftps1;
    public int verbose;
    long transtime;
    long transbytes;
    String command;               // user's command
    String[] argument;            // arguments to the user's command
    String pathname;              // for use with the ls command
    public ftp(String[] args){
        try
        {
            ftps1 = new Ftps();
            ftps1.addFtpsEventListener(new FTPEvents(this));
        }catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        if (args.length >= 3)
        {
            try
            {
                ftps1.setRemoteHost(args[0]);
                ftps1.setUser(args[1]);
                ftps1.setPassword(args[2]);
                ftps1.logon();
            }
            catch(InEDIException e)
            {
                System.out.println( e.getMessage());
                System.exit(e.getCode());
                return;
            }
        }

        if (args.length == 1)
        {
            try
            {
                ftps1.setRemoteHost(args[0]);
                logon();
            }
            catch(InEDIException e)
            {
                System.out.println(e.getMessage());
                System.exit(e.getCode());
                return;
            }
        }

        while (true)
        {
            try
            {
                ftps1.setRemoteFile("");
                System.out.print( "ftp> " );
                command = input();
                argument = command.split("\\s");

                if ( argument[0].equals("?") || argument[0].equals("help") )
                {
                    System.out.println("?        bye     help     put     rmdir");
                    System.out.println("append   cd      ls       pwd     verbose");
                    System.out.println("ascii    close   mkdir    quit");
                    System.out.println("binary   get     open     rm");
                }
                else if ( argument[0].equals("append") )
                {
                    ftps1.setLocalFile(argument[1]);
                    ftps1.setRemoteFile(argument[2]);
                    ftps1.append();
                }
                else if ( argument[0].equals("ascii") )
                {
                    ftps1.setTransferMode(Ftps.tmASCII);
                }
                else if ( argument[0].equals("binary") )
                {
                    ftps1.setTransferMode(Ftps.tmBinary);
                }
                else if ( argument[0].equals("bye") || argument[0].equals("quit"))
                {
                    ftps1.logoff();
                    System.exit(0);
                    return;
                }
                else if (argument[0].equals("close"))
                {
                    ftps1.logoff();
                }
                else if ( argument[0].equals("cd") )
                {
                    if( argument.length > 0)
                        ftps1.setRemotePath(argument[1]);
                }
                else if ( argument[0].equals("get") )
                {
                    if ( argument.length < 3 )
                    {
                        System.out.println("get command requires remotefile and localfile.");
                    }
                    else
                    {
                        ftps1.setRemoteFile(argument[1]);
                        ftps1.setLocalFile(argument[2]);
                        ftps1.download();
                        updateTime();
                    }
                }
                else if ( argument[0].equals("ls") )
                {
                    if ( argument.length > 1 )
                    {
                        pathname = ftps1.getRemotePath();
                        ftps1.setRemotePath(argument[1]);
                        ftps1.listDirectoryLong();
                        ftps1.setRemotePath(pathname);
                    }
                    else
                        ftps1.listDirectoryLong();
                }
                else if ( argument[0].equals("mkdir") )
                {
                    if ( argument.length > 1 )
                        ftps1.makeDirectory(argument[1]);
                }
                else if ( argument[0].equals("mv") )
                {
                    ftps1.setRemoteFile(argument[1]);
                    ftps1.renameFile(argument[1]);
                }
                else if ( argument[0].equals("open") )
                {
                    if (argument.length < 2) {
                        System.out.println("open command requires following hostname.");
                    } else {
                        ftps1.logoff();
                        ftps1.setRemoteHost(argument[1]);
                        logon();
                    }
                }
                else if ( argument[0].equals("passive") )
                {
                    if ( argument.length > 1 )
                    {
                        if ((argument[1].equals("on")) && !ftps1.isPassive())
                        {
                            ftps1.setPassive(true);
                            System.out.println( "Passive mode ON." );
                        }
                        else if ((argument[1].equals("off")) && ftps1.isPassive())
                        {
                            ftps1.setPassive(false);
                            System.out.println( "Passive mode OFF." );
                        }
                    }
                }
                else if ( argument[0].equals("put") )
                {
                    if ( argument.length < 3 )
                    {
                        System.out.println("put command requires localfile and remotefile.");
                    }
                    else
                    {
                        // put localfile remotefile
                        ftps1.setRemoteFile(argument[2]);
                        ftps1.setLocalFile(argument[1]);
                        ftps1.upload();
                        updateTime();
                    }
                }
                else if ( argument[0].equals("pwd") )
                {
                    System.out.println( ftps1.getRemotePath() );
                }
                else if ( argument[0].equals("rm") )
                {
                    if ( argument.length > 1 )
                        ftps1.deleteFile(argument[1]);
                }
                else if ( argument[0].equals("rmdir") )
                {
                    if ( argument.length > 1 )
                        ftps1.removeDirectory(argument[1]);
                }
                else if ( argument[0].equals("verbose") )
                {
                    if ( argument.length > 1 )
                    {
                        if ((argument[1].equals("on")) && verbose == 0)
                        {
                            toggle_verbose();
                        }
                        else if ((argument[1].equals("off")) && verbose == 1)
                        {
                            toggle_verbose();
                        }
                    }
                    else
                    {
                        toggle_verbose();
                    }
                }
                else if ( argument[0].equals("") )
                {
                    // Do nothing
                }
                else {
                    System.out.println( "Bad command / Not implemented in demo." );
                } // end of command checking
            }
            catch(InEDIException e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
                System.exit(e.getCode());
                return;
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
    private void updateTime()
    {
        System.out.print(transbytes);
        System.out.print(" bytes sent in ");
        System.out.print(((float) transtime / 1000));
        System.out.print(" seconds.  (");
        System.out.print(((float) transbytes) / transtime);
        System.out.println("KBps)");
    }
    private String input() throws IOException
    {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        return bf.readLine();
    }

    void logon() throws InEDIException
    {
        String buffer;
        try
        {
            System.out.print("User: " );
            buffer = input();
            ftps1.setUser( buffer );
            System.out.print("Password: ");
            buffer = input();
            ftps1.setPassword( buffer );
            ftps1.logon();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
    public void toggle_verbose()
    {
        verbose = 1 - verbose;
        System.out.print( "Verbose mode " );
        if (verbose == 1)
            System.out.println( "ON." );
        else
            System.out.println( "OFF." );
    }

    public static void main(String[] args)
    {
        new ftp(args);
    }
    public void dirList(FtpsDirListEvent arg) {
        System.out.println(arg.dirEntry);
    }
    public void PITrail(FtpsPITrailEvent arg) {
        if (verbose == 1)
            System.out.println(arg.message);
    }
    public void startTransfer() {
        transtime = System.currentTimeMillis();
    }
    public void transfer(FtpsTransferEvent arg) {
        transbytes = arg.bytesTransferred;
    }
    public void endTransfer() {
        long endtime;
        endtime = System.currentTimeMillis();
        transtime = endtime - transtime;
    }
    public void error(FtpsErrorEvent arg) {
        System.out.println("\nError "+arg.errorCode+": "+arg.description);
    }
    public void SSLServerAuthentication(FtpsSSLServerAuthenticationEvent arg){
        arg.accept = true;
    }
}
class FTPEvents implements FtpsEventListener{
		ftp instance;
    public FTPEvents(ftp instance){
        this.instance = instance;
    }
    public void connectionStatus(FtpsConnectionStatusEvent arg) {
    }
    public void dirList(FtpsDirListEvent arg) {
        instance.dirList(arg);
    }
    public void endTransfer(FtpsEndTransferEvent arg) {
        instance.endTransfer();
    }
    public void error(FtpsErrorEvent arg) {
        instance.error(arg);
    }
    public void PITrail(FtpsPITrailEvent arg) {
    }
    public void startTransfer(FtpsStartTransferEvent arg0) {
        instance.startTransfer();
    }
    public void transfer(FtpsTransferEvent arg) {
        instance.transfer(arg);
    }
    public void SSLServerAuthentication(FtpsSSLServerAuthenticationEvent arg){
        instance.SSLServerAuthentication(arg);
    }
    public void SSLStatus(FtpsSSLStatusEvent arg){}
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



