/**
 *
 */
package com.eis.b2bmb.api.v1.services;

import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.base.test.TestBase;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.UserProfile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author harjeets
 *         Date : 23 Jan 2014
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
public class TestRouteDefService extends TestBase {

    @Autowired
    RouteDefService routeDefService;
    @Autowired
    UserProfileDAO profileDAO;


    @Test
    public void testRouteDefService() throws B2BNotFoundException, B2BTransactionFailed, B2BNotAuthenticatedException, B2BNotAuthorizedException, ValidationException {
        assertNotNull(routeDefService);
        assertNotNull(profileDAO);

        serverSideLogin();

        try{
        	
        	UserProfile profile= profileDAO.getByUserId("mingardia@mycompanyxyz.com");
        	 assertNotNull(profile);
            RouteDef routeDef = new RouteDef();

            routeDef.setId(String.valueOf(UUID.randomUUID()));
            routeDef.setRefName("MyRouteRefName1");
            routeDef.setDataDomain("com.mycompanyxyz");
            routeDef.setRouteDefinition("<routes xmlns=\"http://camel.apache.org/schema/spring\"><route id=\"com.mycompanyxyz.MyRouteRefName1\" " +
                    "autostartup=\"false\">" +
                    "<from uri=\"b2bmbFileSystem://com.mycompanyxyz/somefiles\"/> " +
                    "<to uri=\"b2bmbFileSystem://com.mycompanyxyz/otherfiles\"/></route></routes>");
            routeDef.setVersion("v1.0");
            routeDef.setUserProfileId(profile.getId());

            boolean gotValidationException = false;
            try {
                routeDefService.save(routeDef);
            } catch (ValidationException e) {
                gotValidationException = true;
            }
            assertTrue(gotValidationException);

            //note case is corrected
            routeDef.setRouteDefinition("<routes xmlns=\"http://camel.apache.org/schema/spring\"><route id=\"com.mycompanyxyz.MyRouteRefName1\" " +
                    "autoStartup=\"false\">" +
                    "<from uri=\"b2bmbFileSystem://com.mycompanyxyz/somefiles\"/> " +
                    "<to uri=\"b2bmbFileSystem://com.mycompanyxyz/otherfiles\"/></route></routes>");
            routeDefService.save(routeDef);

            RouteDef dbRouteDef = routeDefService.getById(routeDef.getId());

            assertNotNull(dbRouteDef);

            routeDefService.delete(dbRouteDef.getId());
        }
         finally {
            logout();
        }
    }
}
