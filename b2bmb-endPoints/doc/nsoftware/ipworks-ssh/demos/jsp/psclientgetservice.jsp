<html>
<head>
<title>IP*Works! SSH V9 Demos - PSClient Get-Service Demo</title>
<link rel="stylesheet" type="text/css" href="stylesheet.css">
<meta name="description" content="IP*Works! SSH V9 Demos - PSClient Get-Service Demo"></head>

<body>
<div id="content">
<h1>IP*Works! SSH - Demo Pages</h1>
<h2>PSClient Get-Service Demo</h2>
<p>Shows how to use the PSClient component to list, start, and stop services on a host running PowerShell Server.</p>
<a href="seecode.jsp?psclientgetservice.jsp">[See The Code]</a>
<a href="default.jsp">[Other Demos]</a>
<hr/>

<%@ page import="ipworksssh.IPWorksSSHException,ipworksssh.PSProperty"%>
    <%@ page import="ipworksssh.Psclient" %>
    <%@ page import="java.util.Iterator" %>

    <form method=post>
    <input type="hidden" name="actionname" id="idactionname" />
    <input type="hidden" name="actionarg" id="idactionarg"  />
    <center>
      <table width="90%">
        <tr><td>Server:      <td><input type=text name=server value="<%=request.getParameter("server") == null ? "" : request.getParameter("server") %>" size=40>
        <tr><td>User:        <td><input type=text name=user value="<%=request.getParameter("user") == null ? "DOMAIN\\Username" : request.getParameter("user") %>" size=20>
        <tr><td>Password:    <td><input type=password name=password value="<%=request.getParameter("password") == null ? "" : request.getParameter("password") %>" size=20>
        <tr><td><td><input type=submit value="  Get-Service  ">
      </table>
    </center>
  </form>

  <script>
  function post(action, arg){
    var frm = document.forms[0];
    var eleAction = document.getElementById("idactionname");
    var eleArg = document.getElementById("idactionarg");
    eleAction.value = action;
    eleArg.value = arg;
    frm.submit();
  }
  </script>
  <%
    if (request.getMethod().equalsIgnoreCase("POST")) {

      String actionname = request.getParameter("actionname");
      String actionarg = request.getParameter("actionarg");

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
      if (actionname.equalsIgnoreCase("Start"))
        psclient1.execute("start-service -name '" + actionarg + "'");
      else if (actionname.equalsIgnoreCase("Stop"))
        psclient1.execute("stop-service -name '" + actionarg + "'");
      else if (actionname.equalsIgnoreCase("Restart"))
        psclient1.execute("restart-service -name '" + actionarg + "'");

      psclient1.clearOutput();
    	psclient1.execute("get-service");
    	
    	String serviceName = "";
    	String displayName = "";
    	String status = "";   	
			
			ps.write("<center><table width=\"90%\"><tr><th>ServiceName</th><th>DisplayName</th><th>Status</th><th></th><th></th><th></th></tr>");
			
    	for(int i =0;i<psclient1.getPSObjectCount();i++)
    	{
        	serviceName = "";
        	displayName = "";
        	status = "";
    		
        	psclient1.setPSObjectIndex(i);
        	for ( Iterator e = psclient1.getPSObject().values().iterator(); e.hasNext(); ) 
        	{
             PSProperty prop = (PSProperty)e.next();
             if(prop.getName().equals("ServiceName"))
               serviceName = prop.getValue();
             else if (prop.getName().equals("DisplayName"))
               displayName = prop.getValue();
             else if (prop.getName().equals("Status"))
             {
               String statusValue = prop.getValue();
               if (statusValue.equals("1")){
                 status = "Stopped";
               }
               else if (statusValue.equals("4")){
                 status = "Running";
               }
               else{
                 status = statusValue;
               }
             }
        	}
        	
        	ps.write("<tr>");
        	ps.write("<td>" + serviceName + "</td>");
        	ps.write("<td>" + displayName + "</td>");
        	ps.write("<td>" + status + "</td>");
        	ps.write("<td>");
          if (status.equals("Stopped")){
            ps.write("<input type=\"submit\" value=\"Start\" onclick=\"javascript:post('Start','" + serviceName + "');\" />");
          }
        	ps.write("</td>");

          ps.write("<td>");
          if (status.equals("Running")){
            ps.write("<input type=\"submit\" value=\"Stop\" onclick=\"javascript:post('Stop','" + serviceName + "');\" />");
          }
        	ps.write("</td>");

          ps.write("<td><input type=\"submit\" value=\"Restart\" onclick=\"javascript:post('Restart','" + serviceName + "');\" /></td>");
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

