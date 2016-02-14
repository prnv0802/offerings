package com.eis.b2bmb.camel.custom.component;

import com.eis.common.Constants;
import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.dao.ScriptDAO;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.Correlation;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.Script;
import com.eis.core.api.v1.model.UserProfile;
import com.eis.core.api.v1.service.TenancyManagerService;
import com.eis.core.common.JSRunner;
import com.eis.security.multitenancy.model.SecureSession;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
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
 * Consumer for scripts - kicks off a script to run and populate exchange.
 * Batching is not supported; a script can populate at most one exchange per run.
 * No success or failure callback is called on route completion.
 *
 * @author aaldredge
 */
public class B2bmbScriptConsumer extends ScheduledPollConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbScriptConsumer.class);

    //package visibility, default delay if none set
    static final long DEFAULT_CONSUMER_DELAY = 60 * 1000L;


    /**
     * @param endpoint  endpoint object
     * @param processor processor
     */
    public B2bmbScriptConsumer(B2bmbScriptEndpoint endpoint, Processor processor) {

        super(endpoint, processor);
    }

    @Override
    protected int poll() throws Exception {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Calling  B2bmbScriptConsumer   poll() method ");
        }
        ScriptDAO scriptDAO = getEndpoint().getScriptDAO();
        if (scriptDAO == null) {
            throw new IllegalArgumentException("scriptDAO is not being initialized  ");
        }
        CorrelationDAO correlationDAO = getEndpoint().getCorrelationDAO();
        if (scriptDAO == null) {
            throw new IllegalArgumentException("correlationDAO is not being initialized  ");
        }

        UserProfileDAO userProfileDAO = getEndpoint().getUserProfileDAO();

        if (userProfileDAO == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("!!! UserProfileDAO not set for endPoint URL:" + getEndpoint().getEndpointUri());
            }
        }


        // Enable when you want to make this explicitly required
       /* if (userProfileDAO == null)
        {
            throw new IllegalArgumentException("UserProfileDAO could not be initialized");
        } */

        Script script = scriptDAO.getByRefName(getEndpoint().getScriptName(), getEndpoint().getDomain());
        if (script != null) {
            boolean populatedExchange = false;
            Exchange exchange = getEndpoint().createExchange();

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
                        for (DynamicAttribute attribute : script.getType().getInputs().getAttributes().values()) {
                            String attributeName = attribute.getRefName();
                            boolean required = attribute.isRequired();

                            if (attributeName != null) {
                                if (getEndpoint().getParameters() != null) {
                                    if (getEndpoint().getParameters().get(attributeName) != null) {
                                        attribute.setValue(getEndpoint().getParameters().get(attributeName));
                                    }
                                    else if (required) {
                                        throw new IllegalStateException("Variable:"
                                                + attributeName
                                                + " is required by Script:"
                                                + script.getRefName()
                                                + " but variable was not found in route context");
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
                                throw new B2BNotFoundException("the user with userId:" + script.getRunAsId()
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


                            SecureSession.init("RUNT SCRIPT", Constants.CANTATA_APP_NAME, "127.0.0.1", "SCRIPT",
                                    String.valueOf(UUID.randomUUID()), String.valueOf(UUID.randomUUID()),dbu);

                            SecureSession.setUser(dbu);

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

                        for (DynamicAttribute attribute : returnScript.getType().getOutputs().getAttributes().values
                                ()) {

                            String attributeName = attribute.getRefName();

                            if (attributeName != null) {

                                if ("body".equals(attributeName)) {
                                    exchange.getIn().setBody(attribute.getValue());
                                    populatedExchange = true;
                                }
                                else if (attributeName.startsWith("header_")) {
                                    String headerKey = attributeName.substring("header_".length());
                                    exchange.getIn().setHeader(headerKey, attribute.getValue());
                                    populatedExchange = true;
                                }
                                else if (attributeName.startsWith("property_")) {
                                    String propertyKey = attributeName.substring("property_".length());
                                    exchange.setProperty(propertyKey, attribute.getValue());
                                    populatedExchange = true;
                                }
                                else {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn(">>> ### Could not find attributeName:" + attributeName
                                                + "attribute does not the body, or start with header_, or property_");
                                    }

                                    if (attribute.isRequired()) {
                                        throw new IllegalStateException("Required attribute:" + attributeName +
                                                " does start header_, property_, or named body check attribute naming");
                                    }
                                }
                            }
                        }
                    }

                    if (populatedExchange) {
                        //TODO: look at adding success and failure callbacks as endpoint parameters; can use consumer.<>
                        getProcessor().process(exchange);
                        return 1;
                    }
                    else {
                        return 0;
                    }

                }
                else {
                    throw new IllegalStateException("An attempt call blacklisted script:" + script.getRefName() +
                            " from route:" + exchange.getFromRouteId() + " was made.");
                }

            } catch (ScriptException se) {
                String msg = "There was a problem executing script:" + script.getRefName() + " from route:" +
                        exchange.getFromRouteId() +
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
        }
        else {
            throw new B2BNotFoundException("Script " + script + "was not found.");
        }
    }

    @Override
    public B2bmbScriptEndpoint getEndpoint() {

        return (B2bmbScriptEndpoint) super.getEndpoint();
    }

    @Override
    public String toString() {

        return "B2bmbScriptConsumer";
    }
}

