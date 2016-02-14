package com.eis.b2bmb.api.v1.services;

/**
 * Created by Nagaraj T on 5/1/15.
 */

import com.eis.b2bmb.api.v1.model.EmailMessage;
import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.base.test.TestBase;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.FileSystemEntry;
import com.eis.core.api.v1.model.FileSystemEntryType;
import com.eis.core.api.v1.model.UserProfile;
import com.eis.core.api.v1.service.FileSystemEntryService;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/springContext.xml"})
public class TestEmailMessageService extends TestBase {

    private static final Logger LOG = LoggerFactory.getLogger(TestEmailMessageService.class);
    private static final String TEMPLATE_FILE_NAME ="salesorder_html.vm";

    @Autowired
    EmailMessageService emailMessageService;

    @Autowired
    FileSystemEntryService fileSystemEntryService;

    @Autowired
    RouteDefService routeDefService;

    @Autowired
    UserProfileDAO userProfileDAO;

    protected void logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
    }

    @Test
    public void testEmailSendingRoute() {
        try {
            final String refName = "TestEmailRoute";
            final String dataDomain = "com.mycompanyxyz";

            serverSideLogin();


            createTestTemplateInMongoFS(TEMPLATE_FILE_NAME,dataDomain);
            // construct Email Message bean and populate
            EmailMessage emailMessage = new EmailMessage();
            emailMessage.setServerName("smtp.gmail.com");
            emailMessage.setPort("465");
            emailMessage.setUserName("testdummyuser1");
            emailMessage.setPassword("gt!@#$%^");
            emailMessage.setToAddress("ntadipatri@envistacorp.com");
            emailMessage.setFromAddress("orders@b2mb.eis.com");
            emailMessage.setSubject("You super cool order Email");
            emailMessage.setTemplateName(TEMPLATE_FILE_NAME);
            emailMessage.setContentType("text/html");

            Map<String,String> templateNameValues = new HashMap<String,String>();

            templateNameValues.put("orderNo","1001");
            templateNameValues.put("customerId","123456");
            templateNameValues.put("orderDate","January 10,2015");
            templateNameValues.put("companyAddress","4567 Main St. <br>Buffalo, NY, 98052<br>555-0111");
            templateNameValues.put("billToAddress","Zeyad Rajabi<br>Adventure Works<br>424 Main St.<br>Buffalo. NY, 98052<br>555-0101");
            templateNameValues.put("shipToAddress","Wedy Kahn, <br>Coho Vineayard<br>500 Main St.<br>Buffalo, NY 98044<br>555-0999");

            List list = new ArrayList();
            Map map = new HashMap();

            map.put("qty", "1.00");
            map.put("item", "100");
            map.put("description", "Mountain Bike");
            map.put("unitPrice", "$ 450.00");
            map.put("discount", "$ 25.00");
            map.put("lineTotal", "$ 425.00");
            list.add( map );

            map = new HashMap();
            map.put("qty", "2.00");
            map.put("item", "555");
            map.put("description", "Helmet");
            map.put("unitPrice", "$ 35.00");
            map.put("discount", "");
            map.put("lineTotal", "$ 70.00");
            list.add( map );

            emailMessage.setLineItemValues(list);
            emailMessage.setTemplateNameValues(templateNameValues);
            emailMessage.setRouteId(refName);

            RouteDef route = createTestRoute(dataDomain,emailMessage);
            // call send email
            emailMessageService.sendEmail(emailMessage);
            logout();
            Thread.sleep(1000);
            serverSideLogin();


        } catch (Exception ex) {
            if (LOG.isErrorEnabled())
                LOG.error("Exception Occurred while executing testEmailSendingRoute ********** ", ex);
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param templateName - templateName
     * @param dataDomain - dataDomain
     * @throws com.eis.core.api.v1.exception.ValidationException  - ValidationException
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException  - B2BNotFoundException
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed   - B2BTransactionFailed
     * @throws com.eis.core.api.v1.exception.B2BNotAuthenticatedException  - B2BNotAuthenticatedException
     * @throws com.eis.core.api.v1.exception.B2BNotAuthorizedException   - B2BNotAuthorizedException
     */
    private void createTestTemplateInMongoFS(final String templateName, final String dataDomain)
            throws ValidationException, B2BNotFoundException, B2BTransactionFailed, B2BNotAuthenticatedException,
            B2BNotAuthorizedException {
        String templateString = "";
        /*
        String templateString = "Dear ${lastName}, ${firstName}\n " +
                " Thanks for your Order.\n" +
                "${body}";
         */
        VelocityEngine ve = new VelocityEngine();
        ve.init();

        VelocityContext context = new VelocityContext();
        Template t = ve.getTemplate(TEMPLATE_FILE_NAME);

        StringWriter writer = new StringWriter();
        t.merge( context, writer );

        templateString = writer.toString();


        // save Template into Mongo File System (Grid FS) along with domain.
        FileSystemEntry templateFolder = new FileSystemEntry();

        templateFolder.setRefName("templateFolder");
        templateFolder.setName("templateFolder");
        templateFolder.setType(FileSystemEntryType.Directory);

        FileSystemEntry savedObj = fileSystemEntryService.save(templateFolder);
        InputStream in = null;
        try {
            in = IOUtils.toInputStream(templateString, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String contentType = "application/octet-stream; name=" + TEMPLATE_FILE_NAME;
        fileSystemEntryService.uploadFile(templateName, savedObj.getId(), in, contentType, dataDomain);
    }

    /**
     *
     *
     * @param dataDomain - dataDomain
     * @param emailMessage - email message
     * @return routeDef - routeDef
     * @throws com.eis.core.api.v1.exception.ValidationException  - validation exception
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException  - B2BNotFoundException
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed  - B2BTransactionFailed
     * @throws com.eis.core.api.v1.exception.B2BNotAuthenticatedException    - B2BNotAuthenticatedException
     * @throws com.eis.core.api.v1.exception.B2BNotAuthorizedException     - B2BNotAuthenticatedException
     */
    protected RouteDef createTestRoute(final String dataDomain, EmailMessage emailMessage)
            throws ValidationException,B2BNotFoundException, B2BTransactionFailed, B2BNotAuthenticatedException,
            B2BNotAuthorizedException {


        String serverName = emailMessage.getServerName();
        String serverPort = emailMessage.getPort();
        String userName = emailMessage.getUserName();
        String password = emailMessage.getPassword();
        String refName = emailMessage.getRouteId();



        String routeStr = "<routes xmlns=\"http://camel.apache.org/schema/spring\">\n" +
                "<route id=\""+dataDomain+"."+refName+"\" autoStartup=\"false\">\n" +
                "    <from uri=\"activemq:queue:send.email.queue\"/>\n" +
                "    <process ref=\"emailProcessor\"/>\n" +
                "    <to uri=\"smtps://"+serverName+":"+serverPort+"?username="+userName+"&amp;" +
                "password="+password+"\"/>\n" +
                "</route>\n" +
                "</routes>";

        if (LOG.isInfoEnabled()) {
            LOG.info("routeStr  "+routeStr);
        }
        UserProfile profile = userProfileDAO.getByUserId("mingardia@mycompanyxyz.com");
        boolean gotValidationException = false;
        RouteDef routeDef = null;
        try {
            // check for existing route with refName and dataDomain
            routeDef = routeDefService.getByRefName(refName,dataDomain);

            // If a routeDef already exists show message else create a new route
            if(routeDef == null) {
                routeDef = new RouteDef();
                routeDef.setId(String.valueOf(UUID.randomUUID()));
                routeDef.setRefName(refName);
                routeDef.setDataDomain(dataDomain);
                routeDef.setRouteDefinition(routeStr);
                routeDef.setVersion("v1.0");
                routeDef.setUserProfileId(profile.getId());
                routeDef = routeDefService.save(routeDef);
            }
            else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Route with RefName " + refName + " and Data Domain   " + dataDomain + " already exists.");
                }
            }
        } catch (ValidationException e) {
            gotValidationException = true;
            e.printStackTrace();
        }

        return routeDef;
    }

}
