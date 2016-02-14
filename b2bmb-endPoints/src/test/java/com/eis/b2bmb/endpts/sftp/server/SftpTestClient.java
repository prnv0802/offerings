package com.eis.b2bmb.endpts.sftp.server;

import com.jcraft.jsch.*;

import java.io.*;

public class SftpTestClient implements Runnable{
  public static void main(String[] args){
     Thread t1 = new Thread(new SftpTestClient());
     Thread t2 = new Thread(new SftpTestClient());
     Thread t3 = new Thread(new SftpTestClient());
     t1.start();
     t2.start();
     t3.start();
	 // new SftpTestClient().run();
  }
  
  private void execute(Session session, int i){
	  try{

	      ChannelSftp c = (ChannelSftp) session.openChannel("sftp");
	      c.connect();
	      if(c.ls("/home/abhishek"+i).contains("test")){
	    	  c.rmdir("test");
	      }
		  c.mkdir("test");
		  System.out.println("Directory created >> test for user ::"+i);
		  Thread.sleep(500);
	      c.cd("test");
	      System.out.println("Changed Directory to test for user::"+i);
	      Thread.sleep(500);
	      c.put("D:\\Envista\\client-doc\\DOC\\B2BMailboxSpec.pptx", "/home/abhishek"+i+"/test/");
	      System.out.println("File Uploaded for user::"+i);
	      Thread.sleep(500);
	      File file = new File("C:\\MyTemp\\B2BMailboxSpec.pptx");
	      if(file.exists()){
	    	  file.delete();
	      }
	      System.out.println("temp directory cleanned");
	      c.get("/home/abhishek"+i+"/test/B2BMailboxSpec.pptx", "C:\\MyTemp\\");
	      System.out.println("File downloaded to temp directory for user::"+i);
	      Thread.sleep(500);
	      c.cd("/. .");
	      c.rmdir("test");
	      System.out.println("test directory deleted .. for user ::"+i);
	      Thread.sleep(500);
	      System.out.println("============End Loop==========");
	      Thread.sleep(500);
	      c.disconnect();
	    }
	    catch(Exception e){
	      System.out.println(e);
	    }
  }

  public static class MyUserInfo implements UserInfo{
	public MyUserInfo(String passwd){
		this.passwd = passwd;
	}
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){
    	str = "Yes";
		return true;}
  
    String passwd;

    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public boolean promptPassword(String message){
        return true;
    }
    public void showMessage(String message){
   
    }
    }

	@Override
	public void run() {
		synchronized (this) {
				
			JSch jsch=new JSch();  
	        String host="172.16.9.37"; // enter username and ipaddress for machine you need to connect
		    Session session = null;
			try {
				for(int i=4; i<14; i++){
					String user = "test1.test1";
					String pass = "test1";
					user = user.replace("1", ""+i);
					pass = pass.replace("1", ""+i);
					session = jsch.getSession(user, host, 2299);
				    UserInfo ui=new MyUserInfo(pass);
			        session.setUserInfo(ui);
			        session.connect();	
					execute(session,i);
					session.disconnect();
				}
			} catch (JSchException e) {
				   System.out.println("Could not able to connect to the server >> Caused By:: "+e.getMessage());
				   e.printStackTrace();
			}
		}
	}
  }
