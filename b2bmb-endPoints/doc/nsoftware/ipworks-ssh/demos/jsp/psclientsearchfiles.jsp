<html>
<head>
<title>IP*Works! SSH V9 Demos - PSClient Search Files Demo</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="IP*Works! SSH V9 Demos - PSClient Search Files Demo"></head>

<body>
<div id="content">
<h1>IP*Works! SSH - Demo Pages</h1>
<h2>PSClient Search Files Demo</h2>
<p>This demo shows how to the use the PSClient component to search files in a specified path for a given search term on a remote machine that is running the PowerShell Server.</p>
<a href="seecode.jsp?psclientsearchfiles.jsp">[See The Code]</a>
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
        <tr><td>Search Path:       <td><input type=text name=searchpath value="c:\" size=40>
        <tr><td>Search Filter:     <td><input type=text name=searchfilter value="*.txt" size=5> Recurse: <input type=checkbox name=recurse>
        <tr><td>Search Term:       <td><input type=text name=searchterm value="test" size=40>        	
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
		  
    	psclient1.setSSHUser(request.getParameter("user"));
    	psclient1.setSSHPassword(request.getParameter("password"));
    	
    	String commandToExecute = "gci ";
    	
    	if(request.getParameter("recurse") != null && request.getParameter("recurse").equals("on"))
    	{
    	  commandToExecute += "-recurse -path \"" + request.getParameter("searchpath") + "\" -include " + request.getParameter("searchfilter");
    	}
      else
    	{
    	  commandToExecute += "-path \"" + request.getParameter("searchpath") + "\\" + request.getParameter("searchfilter") + "\"";
    	}
    	
    	commandToExecute += " | select-string \"" + request.getParameter("searchterm") + "\"";
    	
    	
    	psclient1.SSHLogon(request.getParameter("server"), 22);
    	psclient1.execute(commandToExecute);
    	
    	String pathName = "";
    	String lineNum = "";
    	String lineText = "";
			
			ps.write("<center><table width=\"90%\"><tr><th>Path</th><th>Line Num</th><th>Line Text</th></tr>");
			
    	for(int i =0;i<psclient1.getPSObjectCount();i++)
    	{
        	pathName = "";
        	lineNum = "";
        	lineText = "";
    		
        	psclient1.setPSObjectIndex(i);
        	
        	for ( Iterator e = psclient1.getPSObject().values().iterator(); e.hasNext(); ) 
        	{
	               PSProperty prop = (PSProperty)e.next();
	               if(prop.getName().equals("LineNumber"))
	            	   lineNum = prop.getValue();
	               else if (prop.getName().equals("Line"))
	            	   lineText = prop.getValue();
	               else if (prop.getName().equals("Path"))
	            	   pathName = prop.getValue();
        	}
        	
        	ps.write("<tr>");
        	ps.write("<td>" + pathName + "</td>");
        	ps.write("<td>" + lineNum + "</td>");
        	ps.write("<td>" + lineText + "</td>");
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

