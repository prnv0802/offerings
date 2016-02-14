package com.eis.b2bmb.camel.custom.component;

import com.eis.common.Constants;
import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.dao.ScriptDAO;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.core.api.v1.service.TenancyManagerService;
import com.eis.core.common.JSRunner;
import com.eis.security.multitenancy.model.SecureSession;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Camel producer for Scripts
 *
 * @author aaldredge
 */
public class B2bmbScriptProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbScriptProducer.class);

    /**
     * Constructor
     * @param endpoint   endPoint instance
     */
    public B2bmbScriptProducer(B2bmbScriptEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * @param exchange created exchange
     * @throws Exception thrown by camel
     */
    public void process(Exchange exchange) throws Exception {
        ScriptDAO scriptDAO = getEndpoint().getScriptDAO();
        if (scriptDAO == null) {
            throw new IllegalArgumentException("scriptDAO is not being initialized  ");
        }
        CorrelationDAO correlationDAO = getEndpoint().getCorrelationDAO();
        if (scriptDAO == null) {
            throw new IllegalArgumentException("correlationDAO is not being initialized  ");
        }

        UserProfileDAO userProfileDAO = getEndpoint().getUserProfileDAO();

        Script script = scriptDAO.getByRefName(getEndpoint().getScriptName(), getEndpoint().getDomain());
        if (script != null) {
            boolean exceptionOccurred = false;
            try {
                if (!script.isBlacklisted()) {
                    //I guess should do this?
                    List<String> blackListedPackages = new ArrayList<String>();
                    Correlation correlation = correlationDAO.getByRefName("BlackListScriptPackages",
                            Constants.B2BMAILBOX_APP_DATADOMAIN);
                    if (correlation != null) {
                        blackListedPackages = (List<String>) correlation.getValue();
                    }
                    //set inputs
                    if (script.getType() != null && script.getType().getInputs() != null
                            && script.getType().getInputs().getAttributes() != null) {

                        for (DynamicAttribute attribute: script.getType().getInputs().getAttributes().values()) {
                            String attributeName = attribute.getRefName();

                            if (attributeName != null) {
                                if ("body".equalsIgnoreCase(attributeName)) {

                                    if (DynamicAttributeType.String.equals(attribute.getType()) ||
                                            DynamicAttributeType.Text.equals(attribute.getType())) {
                                        attribute.setValue(exchange.getIn().getBody(String.class));
                                    } else if (DynamicAttributeType.Object.equals(attribute.getType())){

                                        attribute.setValue(exchange.getIn().getBody());

                                    } else {

                                        //the others are weird for a body, so not supported
                                        throw new UnsupportedOperationException("Casting of exchange body to " +
                                                attribute.getType() + " not supported.");

                                    }
                                } else if (attributeName.toLowerCase().startsWith("header_")) {

                                    String headerKey = attributeName.substring("header_".length());
                                    attribute.setValue(exchange.getIn().getHeader(headerKey));

                                } else if (attributeName.toLowerCase().startsWith("property_")) {

                                    String propertyKey = attributeName.substring("property_".length());
                                    attribute.setValue(exchange.getProperty(propertyKey));

                                } else if (getEndpoint().getParameters() != null
                                        && getEndpoint().getParameters().get(attributeName) != null) {

                                    attribute.setValue(getEndpoint().getParameters().get(attributeName));

                                }
                                else
                                {
                                    if (attribute.isRequired())
                                    {
                                        throw new IllegalStateException("Attribute:" + attribute.getRefName() +
                                                " could not be resolved to a header or property value make sure" +
                                        " your variables start with header_ or property_");
                                    }
                                    else
                                    {
                                        if (LOG.isWarnEnabled())
                                        {
                                            LOG.warn("Attribute:" + attribute.getRefName() +
                                                    " could not be resolved to a header or property value make sure" +
                                                    " your variables start with header_ or property_." +
                                            "attribute is not required so ignoring");
                                        }
                                    }
                                }
                            }
                        }
                    }

                    boolean useRunAs = false;

                    if (script.getRunAsId() != null &&
                            userProfileDAO != null) {
                        useRunAs = true;
                    }

                    //Subject s = SecurityUtils.getSubject();
                    ThreadState threadState = null;
                    Script returnScript = null;
                    try {
                        if (useRunAs) {
                            PrincipalCollection pc = new SimplePrincipalCollection(script.getRunAsId(),
                                    "MultiTenantRealm");

                            Subject s = new Subject.Builder().principals(pc).sessionCreationEnabled(true)
                                    .authenticated(true).buildSubject();
                            threadState = new SubjectThreadState(s);
                            threadState.bind();

                            if (LOG.isInfoEnabled()) {
                                LOG.info("Running script under with RunAs with Id:" + script.getRunAsId());
                            }

                            UserProfile dbu = userProfileDAO.getByUserId(script.getRunAsId());
                            if (dbu == null) {
                                throw new B2BNotFoundException("the user with id:" + script.getRunAsId()
                                        + " was not found ");
                            }


                            ArrayList<String> principles = new ArrayList();
                            principles.add(script.getRunAsId());
                            PrincipalCollection principalCollection = new SimplePrincipalCollection(principles,
                                    "MultiTenantRealm");

                            s.getSession(true);



                            s.runAs(principalCollection);



                            s.getSession().setAttribute("userProfile", dbu);
                            s.getSession().setAttribute("defaultDataDomain", dbu.getDataDomain());


                            SecureSession.init("RUN SCRIPT", Constants.CANTATA_APP_NAME, "127.0.0.1", "SCRIPT",
                                    String.valueOf(UUID.randomUUID()), String.valueOf(UUID.randomUUID()),dbu);

                            TenancyManagerService tm = getEndpoint().getTenancyManagerService();

                            SecureSession.setSecurityManager(tm);
                            SecureSession.setAction("RUN SCRIPT");
                        }
                        else {

                            if (LOG.isWarnEnabled()) {
                                LOG.warn(">> Running script with no associated userProfile, service calls will fail");

                            }
                        }

                        JSRunner runner = new JSRunner();
                        returnScript = runner.runJS(script, blackListedPackages);

                    } finally {


                        if (useRunAs) {
                            Subject s = SecurityUtils.getSubject();
                            s.releaseRunAs();
                            s.logout();
                            SecureSession.clearAll();
                        }

                        if (threadState != null) {
                            threadState.restore();
                        }

                    }


                    //set outputs
                    if (returnScript.getType() != null && returnScript.getType().getOutputs() != null
                            && returnScript.getType().getOutputs().getAttributes() != null) {
                        for (DynamicAttribute attribute: returnScript.getType().getOutputs().getAttributes().values()) {
                            String attributeName = attribute.getRefName();
                            if (attributeName != null) {
                                if ("body".equals(attributeName)) {
                                    exchange.getIn().setBody(attribute.getValue());
                                } else if (attributeName.startsWith("header_")) {
                                    String headerKey = attributeName.substring("header_".length());
                                    exchange.getIn().setHeader(headerKey, attribute.getValue());
                                } else if (attributeName.startsWith("property_")) {
                                    String propertyKey = attributeName.substring("property_".length());
                                    exchange.setProperty(propertyKey, attribute.getValue());
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("An attempt call blacklisted script:" + script.getRefName() +
                            " from route:" + exchange.getFromRouteId() + " was made.");
                }

            } catch (ScriptException se) {


                String msg = "### >>>> !!!! There was a problem executing script:" + script.getRefName()
                        + " from route:" +
                        exchange.getFromRouteId()+
                        " problem was:" + se.getMessage();
                if (LOG.isErrorEnabled()) {
                    LOG.error(msg);
                }

                script.setBlacklisted(true);
                script.setBlacklistReason(msg);
                exceptionOccurred = true;
                throw se;
            } finally {
                if (exceptionOccurred) {
                    try {
                        scriptDAO.save(script);

                    } catch (B2BTransactionFailed e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("There was a problem saving the script:" + script.getRefName() + " exception:" +
                                    e.getMessage());
                        }
                    } catch (B2BNotFoundException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("There was a problem saving the script:" + script.getRefName() + " exception:" +
                                    e.getMessage());
                        }
                    } catch (ValidationException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("There was a problem saving the script:" + script.getRefName() + " exception:" +
                                    e.getMessage());
                        }
                    }
                }
            }
        } else {
            throw new B2BNotFoundException("Script " + script + "was not found.");
        }
    }
    @Override
    public B2bmbScriptEndpoint getEndpoint() {
        return (B2bmbScriptEndpoint) super.getEndpoint();
    }
}
