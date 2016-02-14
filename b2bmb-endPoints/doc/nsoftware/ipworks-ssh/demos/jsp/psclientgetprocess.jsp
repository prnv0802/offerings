<html>
<head>
<title>IP*Works! SSH V9 Demos - PSClient Get-Process Demo</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="IP*Works! SSH V9 Demos - PSClient Get-Process Demo"></head>

<body>
<div id="content">
<h1>IP*Works! SSH - Demo Pages</h1>
<h2>PSClient Get-Process Demo</h2>
<p>This demo uses the PSClient component to run the get-process command on a remote machine that is running the PowerShell Server.</p>
<a href="seecode.jsp?psclientgetprocess.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="ipworksssh.IPWorksSSHException,ipworksssh.PSProperty"%>
    <%@ page import="ipworksssh.Psclient" %>
    <%@ page import="java.util.Iterator" %>

    <form method=post>
    <center>
      <table width="90%">
        <tr><td>Server:      <td><input type=text name=server value="" size=40>
        <tr><td>User:        <td><input type=text name=user value="DOMAIN\Username" size=20>
        <tr><td>Password:    <td><input type=password name=password value="" size=20>
        <tr><td><td><input type=submit value="  Get-Process  ">
      </table>
    </center>
  </form>
  <%
    if (request.getMethod().equalsIgnoreCase("POST")) {
      final javax.servlet.jsp.JspWriter ps = out;
      Psclient psclient1 = new Psclient();
      
      psclient1.addPsclientEventListener(new ipworksssh.DefaultPsclientEventListener(){
    	
    	public void SSHServerAuthentication(ipworksssh.PsclientSSHServerAuthenticationEvent e)
    	{
    	  e.accept = true;
    	}
    	});
      
		  try{
		  
			psclient1.setSSHHost(request.getParameter("server"));
    	psclient1.setSSHUser(request.getParameter("user"));
    	psclient1.setSSHPassword(request.getParameter("password"));
    	psclient1.SSHLogon(psclient1.getSSHHost(), 22);
    	psclient1.execute("get-process");
    	
    	String processName = "";
    	String processId = "";
    	String handleCount = "";
    	String nonPagedMem = "";
    	String pagedMem = "";
    	String workingSet = "";
    	String virtualMemSize = "";    	
			
			ps.write("<center><table width=\"90%\"><tr><th>Process</th><th>Id</th><th>Handles</th><th>Paged Mem</th><th>Non Paged Mem</th><th>Virtual Mem</th><th>Working Set</th></tr>");
			
    	for(int i =0;i<psclient1.getPSObjectCount();i++)
    	{
        	processName = "";
        	processId = "";
        	handleCount = "";
    			nonPagedMem = "";
    			pagedMem = "";
    			workingSet = "";        	
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
	               else if (prop.getName().equals("PagedMemorySize"))
	            	   pagedMem = prop.getValue();
	               else if (prop.getName().equals("NonpagedSystemMemorySize"))
	            	   nonPagedMem = prop.getValue();	            	   	            	   
	               else if (prop.getName().equals("VirtualMemorySize"))
	            	   virtualMemSize = prop.getValue();	               
	               else if (prop.getName().equals("WorkingSet"))
	            	   workingSet = prop.getValue();	            	   	        	            	   
        	}
        	
        	ps.write("<tr>");
        	ps.write("<td>" + processName + "</td>");
        	ps.write("<td>" + processId + "</td>");
        	ps.write("<td>" + handleCount + "</td>");
        	ps.write("<td>" + pagedMem + "</td>");
        	ps.write("<td>" + nonPagedMem + "</td>");
        	ps.write("<td>" + virtualMemSize + "</td>");
        	ps.write("<td>" + workingSet + "</td>");
        	ps.write("</tr>");
    	}
			
			ps.write("</table></center>");
		}
		catch(IPWorksSSHException exc)
		{
			ps.write("<b><font color='red'>ERROR: " + exc.getMessage() + "</font></b>");
		}
		finally{
			psclient1.SSHLogoff();
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

