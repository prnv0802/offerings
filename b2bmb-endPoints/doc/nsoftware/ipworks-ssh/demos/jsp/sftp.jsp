<html>
<head>
<title>IP*Works! SSH V9 Demos - SFTP Client</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="IP*Works! SSH V9 Demos - SFTP Client"></head>

<body>
<div id="content">
<h1>IP*Works! SSH - Demo Pages</h1>
<h2>SFTP Client</h2>
<p>A full featured SFTP client built using the SFTP component.  It allows browsing of directories, uploads and downloads of files, and more.</p>
<a href="seecode.jsp?sftp.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="ipworksssh.*"%>

  <form method=post>
    <center>
      <table width="90%">
        <tr><td>Server:      <td><input type=text name=server value="server" size=40>
        <tr><td>User:        <td><input type=text name=user value="user" size=20>
        <tr><td>Password:    <td><input type=password name=password value="password" size=20>
        <tr><td><td><input type=submit value="  Go!  ">
      </table>
    </center>
  </form>
<%
class SftpDirList extends Sftp {
	public SftpDirList(final javax.servlet.jsp.JspWriter ps) {
	try {
		addSftpEventListener(new ipworksssh.SftpEventListener() {	
		public void connected(SftpConnectedEvent e){}
		public void connectionStatus(SftpConnectionStatusEvent e){}
		public void dirList(SftpDirListEvent e){}
		public void disconnected(SftpDisconnectedEvent e){}
		public void endTransfer(SftpEndTransferEvent e){}
		public void error(SftpErrorEvent e){}
		public void SSHCustomAuth(ipworksssh.SftpSSHCustomAuthEvent e){}		
		public void SSHKeyboardInteractive(ipworksssh.SftpSSHKeyboardInteractiveEvent e) {}
		public void SSHServerAuthentication(SftpSSHServerAuthenticationEvent e){
			e.accept = true;
		}
		public void SSHStatus(SftpSSHStatusEvent e){}
		public void startTransfer(SftpStartTransferEvent e){}
		public void transfer(SftpTransferEvent e){}
	}); }
	catch (Exception listenError) {
		}	
	}
}

    if (request.getMethod().equalsIgnoreCase("POST")) {
      final javax.servlet.jsp.JspWriter ps = out;
      SftpDirList sftp = new SftpDirList(ps);
      try{
      	sftp.setSSHHost(request.getParameter("server"));
      	sftp.setSSHUser(request.getParameter("user"));
      	sftp.setSSHPassword(request.getParameter("password"));
      
      	sftp.SSHLogon(sftp.getSSHHost(),22);
 		    sftp.listDirectory();
	      ps.write("<center><table width=\"90%\"><tr><th>Filename</th><th>FileSize</th><th>FileTime</th></tr>");
	      
				for(int i=0;i<sftp.getDirList().size();i++)
				{
					ps.write("<tr>");
					ps.write("<td>" + sftp.getDirList().item(i).getFileName());
					if(sftp.getDirList().item(i).getIsDir())
					{
						ps.write(" &lt;dir&gt;");
					}
					ps.write("<td>" + String.valueOf(sftp.getDirList().item(i).getFileSize()));
					ps.write("<td>" + sftp.getDirList().item(i).getFileTime());
					ps.write("</tr>");
				}
			
				ps.write("</table></center>");	      
      }
      catch(IPWorksSSHException exc)
      {
      	ps.write("<b><font color='red'>ERROR: " + exc.getMessage() + "</font></b>");
      }
      finally{
      	sftp.SSHLogoff();
      }
    }
%>
<br/>
<br/>
<br/>
<hr/>
NOTE: These pages are simple demos, and by no means complete applications.  They
are intended to illustrate the usage of the IP*Works! SSH objects in a simple,
straightforward way.  What we are hoping to demonstrate is how simple it is to
program with our components.  If you want to know more about them, or if you have
questions, please visit <a href="http://www.nsoftware.com/?demopg-IHIP*Works! SSH9V" target="_blank">www.nsoftware.com</a> or
contact our technical <a href="http://www.nsoftware.com/support/">support</a>.
<br/>
<br/>
Copyright (c) 2013 /n software inc. - All rights reserved.
<br/>
<br/></div>

<div id="footer">
<center>
IP*Works! SSH V9 - Copyright (c) 2013 /n software inc. - All rights reserved. - For more information, please visit our website at <a href="http://www.nsoftware.com/?demopg-IHJ9V" target="_blank">www.nsoftware.com</a>.</center></div>
</body></html>

