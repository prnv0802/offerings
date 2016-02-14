package com.eis.core.common;

import com.eis.common.Constants;
import com.eis.core.api.v1.dao.BaseDataAccessFeatures;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.*;
import com.eis.core.api.v1.model.Script;
import com.eis.core.api.v1.service.Service;
import com.eis.core.api.v1.service.TenancyManagerService;
import com.eis.security.multitenancy.model.SecureSession;
import com.eis.spring.util.SpringApplicationContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Class used to run javascript using Rhino.
 * <p>
 * Date: 12/13/13
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JSRunner.class);

    @Autowired
    UserProfileDAO userProfileDAO;

    @Autowired
    TenancyManagerService tenancyManager;

    /**
     * Constructor.
     */
    public JSRunner() {
        if (!ContextFactory.hasExplicitGlobal()) {
            ContextFactory.initGlobal(new EnvistaContextFactory());
        }
    }

    /**
     * Runs the javascript script as a particular user and returns a result.
     *
     * @param script     - Script object containing script and inputs
     * @param blPackages - List<String> of packages that are not allowed by the script runner
     * @return Script containing the results
     * @throws javax.script.ScriptException                               - if script execution fails
     * @throws com.eis.core.api.v1.exception.B2BNotAuthenticatedException - if the user not authenticated
     * @throws com.eis.core.api.v1.exception.B2BNotAuthorizedException    - if the user is not authorized
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed         - if there is a problem running the script
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException         - if data objects are not found
     */
    public Script runJSWithUserAs(Script script, List<String> blPackages) throws B2BNotAuthorizedException,
            B2BNotFoundException, B2BTransactionFailed, B2BNotAuthenticatedException, ScriptException {

        ThreadState threadState = null;
        Script returnScript = null;
        boolean useRunAs = script.getRunAsId() != null;

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


                SecureSession.init("RUNT SCRIPT", Constants.CANTATA_APP_NAME, "127.0.0.1", "SCRIPT",
                        String.valueOf(UUID.randomUUID()), String.valueOf(UUID.randomUUID()), dbu);

                SecureSession.setSecurityManager(tenancyManager);
                SecureSession.setAction("RUN SCRIPT");


            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(">> Running script with no associated userProfile, service calls will fail");

                }
            }

            returnScript = runJS(script, blPackages);

        } finally {

            if (useRunAs) {
                Subject s = SecurityUtils.getSubject();
                s.releaseRunAs();
                SecureSession.clearAll();
            }

            if (threadState != null) {
                //  threadState.restore();  seems to break things
                threadState.clear();
            }

        }

        return returnScript;

    }


    /**
     * Runs javascript and returns a result.
     *
     * @param script     - Script object containing script and inputs
     * @param blPackages - List<String> of packages that are not allowed by the script runner
     * @return Script containing the results
     * @throws javax.script.ScriptException                               - if script execution fails
     * @throws com.eis.core.api.v1.exception.B2BNotAuthenticatedException - if the user not authenticated
     * @throws com.eis.core.api.v1.exception.B2BNotAuthorizedException    - if the user is not authorized
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed         - if there is a problem running the script
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException         - if data objects are not found
     */
    public Script runJS(Script script, List<String> blPackages) throws B2BNotAuthorizedException, B2BNotFoundException,
            B2BTransactionFailed, B2BNotAuthenticatedException, ScriptException {

        Context cx = ContextFactory.getGlobal().enterContext();
        cx.getWrapFactory().setJavaPrimitiveWrap(false);
        Scriptable scope = cx.initStandardObjects();
        Script returnScript = null;
        try {
            cx.setClassShutter(new EnvistaClassShutter(script.getScriptSecurityPolicy(), blPackages));
        } catch (SecurityException e) {
            //will get this security exception when a script calls a script.
            // Ignore exception, stick with original shutter
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received security exception when setting shutter - " +
                        "not the only one on this thread using script", e);
            }
        }
        try {
            //if (checkPermissions(script.getScriptSecurityPolicy())) {
            prepareContext(script, scope);
            convertInputsToScope(scope, script.getInputs());
            Object obj = cx.evaluateString(scope, script.getScript(), "script", 1, null);
            returnScript = convertResult(script, scope, obj);

            // } else {
            //throw new ScriptException("There is an error with your script, user does not have required permission" +
            // " to run this script");
            //}
        } catch (EcmaError e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Caught EmcaError.  Typically this points to a problem in the script. " + e.details(), e);
            }
            throw new ScriptException(e.getMessage());
        } catch (WrappedException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Caught WrappedException.  Typically this points to a problem encountered executing " +
                        "code from the script. " + e.getMessage() + " " + e.getScriptStackTrace(), e);
            }
            if (e.getWrappedException() instanceof RuntimeException) {
                throw (RuntimeException) e.getWrappedException();
            } else {
                //don't want script b/c script exceptions count toward blacklisting...this is a runtime or data problem
                throw new RuntimeException(e.getWrappedException().getMessage(), e);
            }

        } finally {
            cx.exit();
        }
        return returnScript;
    }


    /**
     * Runs javascript and returns a result.
     *
     * @param script     - Script object containing script and inputs
     * @param blPackages - List<String> of packages that are not allowed by the script runner
     * @return Script containing the results
     * @throws javax.script.ScriptException                               - if script execution fails
     * @throws com.eis.core.api.v1.exception.B2BNotAuthenticatedException - if the user not authenticated
     * @throws com.eis.core.api.v1.exception.B2BNotAuthorizedException    - if the user is not authorized
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed         - if there is a problem running the script
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException         - if data objects are not found
     */
    public Script testJS(Script script, List<String> blPackages) throws ScriptException, B2BNotFoundException,
            B2BTransactionFailed, B2BNotAuthorizedException, B2BNotAuthenticatedException {
        Context cx = ContextFactory.getGlobal().enterContext();
        cx.getWrapFactory().setJavaPrimitiveWrap(false);
        Scriptable scope = cx.initStandardObjects();
        Script returnScript = null;
        try {
            cx.setClassShutter(new EnvistaClassShutter(script.getScriptSecurityPolicy(), blPackages));
        } catch (SecurityException e) {
            //will get this security exception when a script calls a script.
            // Ignore exception, stick with original shutter
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received security exception when setting shutter - " +
                        "not the only one on this thread using script", e);
            }
        }
        try {
            //if (checkPermissions(script.getScriptSecurityPolicy())) {
            prepareContext(script, scope);
            Object obj = cx.evaluateString(scope, script.getScript(), "script", 1, null);
            returnScript = convertResult(script, scope, obj);
            //} else {
            //throw new ScriptException("There is an error with your script, user does not have required permission" +
            //" to run this script");
            //}

        } finally {
            cx.exit();
        }
        return returnScript;
    }


    private void prepareContext(Script script, Scriptable scope)
            throws ScriptException, B2BNotFoundException, B2BTransactionFailed,
            B2BNotAuthorizedException, B2BNotAuthenticatedException {
        // Eventually we will be able to change inputs based on ScriptType

        if (script.getType() != null) {
            Map<String, Object> contextMap = new HashMap<String, Object>();
            Map<String, DynamicAttribute> attributes = new HashMap<String, DynamicAttribute>();
            if (script.getType().getInputs() != null) {
                attributes = script.getType().getInputs().getAttributes();
            }
            for (ScriptContextObject contextObject : script.getType().getScriptContextObjects().values()) {
                if (contextObject.getType().equals("serviceBean")) {
                    if (contextObject.getServiceName().equals("blobStore")) {
                        BlobStore service = (BlobStore) SpringApplicationContext.getBean(contextObject.getServiceName
                                ());
                        //Object serviceObject = Class.forName(contextObject.getModelName()).cast(service);
                        contextMap.put(contextObject.getName(), service);
                        addToContext(scope, contextObject.getName(), service);
                    } else {
                        Service service = (Service) SpringApplicationContext.getBean(contextObject.getServiceName());
                        //Object serviceObject = Class.forName(contextObject.getModelName()).cast(service);
                        contextMap.put(contextObject.getName(), service);
                        addToContext(scope, contextObject.getName(), service);
                    }

                }
                //TODO:  Remove DAO later
                else if (contextObject.getType().equals("daoBean")) {
                    if (contextObject.getServiceName().equals("blobStore")) {
                        BlobStore service = (BlobStore) SpringApplicationContext.getBean(contextObject.getServiceName
                                ());
                        //Object serviceObject = Class.forName(contextObject.getModelName()).cast(service);
                        contextMap.put(contextObject.getName(), service);
                        addToContext(scope, contextObject.getName(), service);
                    } else {
                        BaseDataAccessFeatures dao = (BaseDataAccessFeatures) SpringApplicationContext.getBean(
                                contextObject.getServiceName());
                        //Object serviceObject = Class.forName(contextObject.getModelName()).cast(service);
                        contextMap.put(contextObject.getName(), dao);
                        addToContext(scope, contextObject.getName(), dao);
                    }

                } else if (contextObject.getType().equals("findById")) {
                    //Object model = Class.forName(contextObject.getModelName()).newInstance();
                    Service service = (Service) contextMap.get(contextObject.getServiceName());
                    String parameterValue = (String) attributes.get(contextObject.getParameterName()).getValue();
                    if (parameterValue == null || parameterValue.isEmpty()) {
                        throw new ScriptException("The input :" + contextObject.getParameterName() + " must have a " +
                                "value to call findById.");
                    }
                    Object model = service.getById(parameterValue);
                    addToContext(scope, contextObject.getName(), model);

                } else if (contextObject.getType().equals("findByRefName")) {
                    //Object model = Class.forName(contextObject.getModelName()).newInstance();
                    Service service = (Service) contextMap.get(contextObject.getServiceName());
                    String parameterValue = (String) attributes.get(contextObject.getParameterName()).getValue();
                    if (parameterValue == null || parameterValue.isEmpty()) {
                        throw new ScriptException("The input :" + contextObject.getParameterName() + " must have a " +
                                "value to call findByRefName.");
                    }
                    Object model = service.getByRefName(parameterValue);
                    addToContext(scope, contextObject.getName(), model);
                } else if (contextObject.getType().equals("scriptInputVariable")) {
                    Object parameterValue = attributes.get(contextObject.getParameterName()).getValue();
                    addToContext(scope, contextObject.getName(), parameterValue);
                } else if (contextObject.getType().equals("dynamicMethod")) {
                    Service service = (Service) contextMap.get(contextObject.getServiceName());
                    java.lang.reflect.Method methodName = null;
                    try {
                        methodName = service.getClass().getMethod(contextObject.getMethodName());
                    } catch (SecurityException e) {
                        throw new ScriptException("Could not get method:" + contextObject.getMethodName() + " in " +
                                "class:" + service.getClass().getName() + ":" + e.getMessage());

                    } catch (NoSuchMethodException e) {
                        throw new ScriptException("Could not get method:" + contextObject.getMethodName() + " in " +
                                "class:" + service.getClass().getName() + ":" + e.getMessage()); // ...
                    }

                    try {
                        Object model = methodName.invoke(service);
                        addToContext(scope, contextObject.getName(), model);
                    } catch (IllegalArgumentException e) {
                        throw new ScriptException("Could not execute method:" + contextObject.getMethodName() + " in " +
                                "class:" + service.getClass().getName() + ":" + e.getMessage());
                    } catch (IllegalAccessException e) {
                        throw new ScriptException("Could not execute method:" + contextObject.getMethodName() + " in " +
                                "class:" + service.getClass().getName() + ":" + e.getMessage());
                    } catch (InvocationTargetException e) {
                        throw new ScriptException("Could not execute method:" + contextObject.getMethodName() + " in " +
                                "class:" + service.getClass().getName() + ":" + e.getMessage());
                    }
                } else if (contextObject.getType().equals("address")) {
                    PhysicalAddress shipToAddress = new PhysicalAddress();
                    if (attributes.get("addressLine1") != null) {
                        shipToAddress.setAddress1((String) attributes.get("addressLine1").getValue());
                    }
                    if (attributes.get("addressLine2") != null) {
                        shipToAddress.setAddress2((String) attributes.get("addressLine2").getValue());
                    }
                    if (attributes.get("addressLine3") != null) {
                        shipToAddress.setAddress3((String) attributes.get("addressLine3").getValue());
                    }
                    if (attributes.get("city") != null) {
                        shipToAddress.setCity((String) attributes.get("city").getValue());
                    }
                    if (attributes.get("state") != null) {
                        shipToAddress.setState((String) attributes.get("state").getValue());
                    }
                    if (attributes.get("zip5") != null) {
                        shipToAddress.setZip5((String) attributes.get("zip5").getValue());
                    }
                    if (attributes.get("latitude") != null) {
                        shipToAddress.getCoordinates().setLatitude((Double) attributes.get("latitude").getValue());
                    }
                    if (attributes.get("longitude") != null) {
                        shipToAddress.getCoordinates().setLongitude((Double) attributes.get("longitude").getValue());
                    }

                    addToContext(scope, contextObject.getName(), shipToAddress);
                } else if (contextObject.getType().equals("javaBean")) {

                    Object bean = SpringApplicationContext.getBean(contextObject.getName());
                    //Object serviceObject = Class.forName(contextObject.getModelName()).cast(service);
                    contextMap.put(contextObject.getName(), bean);
                    addToContext(scope, contextObject.getName(), bean);

                } else {
                    throw new IllegalStateException("ContextObject with type:" + contextObject.getType() +
                            " and name:" + contextObject.getName() + " found but not handled by code");
                }
            }

            addToContext(scope, "builder", Context.javaToJS(new java.lang.StringBuilder("Log:"), scope));
            addToContext(scope, "lineSeperator", "<br/>");

            // MRI: Seemed like this was missing
            convertInputsToScope(scope, script.getInputs());

        } else {

            addToContext(scope, "builder", Context.javaToJS(new java.lang.StringBuilder("Log:"), scope));
            addToContext(scope, "lineSeperator", "<br/>");

            convertInputsToScope(scope, script.getInputs());
        }

    }

    private boolean checkPermissions(ScriptSecurityPolicy scriptSecurityPolicy) throws B2BNotAuthenticatedException,
            B2BNotAuthorizedException {
        boolean allowScript = true;
        Subject subject = SecurityUtils.getSubject();

        if (!subject.isAuthenticated()) {
            throw new B2BNotAuthenticatedException("API requires the caller to authenticated:",
                    B2BSecurityException.REASON_CODE.NOT_AUTHENTICATED);
        }

       /* get the default data domain. */
        String defaultDomain = (String) SecurityUtils.getSubject().getSession().getAttribute("defaultDataDomain");

        if (defaultDomain == null) {
            throw new IllegalStateException("Data domain is required and the default data domain came back as null");
        }

        // Right now if no permissions are set, then allow it to go through
        for (String permission : scriptSecurityPolicy.getPermissionsNeeded().values()) {
            if (!subject.isPermitted(permission + defaultDomain)) {
                throw new B2BNotAuthorizedException("The Subject:" + subject.getPrincipal() + " does not have the " +
                        "privilege: " + permission + ":" + defaultDomain + " refusing request to run script.",
                        B2BSecurityException.REASON_CODE.NOT_AUTHORIZED
                );
            }
        }

        return allowScript;


    }

    /**
     * Converts the result of the executed script.
     *
     * @param script - the javascript
     * @param scope  - scope to use
     * @param result - the rhino result
     * @return Script object with the result
     */
    private Script convertResult(Script script, Scriptable scope, Object result) {

        // *** Have some questions about how things are returned.
        if (result instanceof Wrapper) {
            result = ((Wrapper) result).unwrap();
            script.setResult(result);
        } else if (result instanceof NativeObject) {
            script.setResult(objectToMap((NativeObject) result));
        } else if (result instanceof NativeArray) {
            script.setResult(objectToList((NativeArray) result));
        } else {
            script.setResult(result);
        }

        if (script.getType().getOutputs() != null && script.getType().getOutputs().getAttributes() != null) {
            for (DynamicAttribute attribute : script.getType().getOutputs().getAttributes().values()) {
                if (scope.get(attribute.getRefName(), scope) != null) {
                    //workaround for the way the script engine implements strings
                    if (scope.get(attribute.getRefName(), scope) instanceof ConsString) {
                        attribute.setValue(scope.get(attribute.getRefName(), scope).toString());
                    } else if (scope.get(attribute.getRefName(), scope) instanceof EnvistaNativeJavaObject) {
                        attribute.setValue(((EnvistaNativeJavaObject)scope.get(attribute.getRefName(), scope))
                                .unwrap());
                    }else {
                        attribute.setValue(scope.get(attribute.getRefName(), scope));
                    }
                }
            }
        }

        Object builder = scope.get("builder", scope);
        if (builder != null) {
            if (builder instanceof Wrapper) {
                builder = ((Wrapper) builder).unwrap();
                if (builder instanceof StringBuilder) {
                    StringBuilder tracer = (StringBuilder) builder;
                    script.setScriptTracer(builder.toString());
                }
            }
        }


        return script;

    }

    /**
     * Adds the object passed in to the JS Rhino scope under the
     * name passed in.
     *
     * @param scope  - scope to populate
     * @param name   - name to reference in the context
     * @param object - the object
     */
    protected void addToContext(Scriptable scope, String name, Object object) {
        ScriptableObject.putProperty(scope, name, object);
    }

    /**
     * Adds the script inputs defined as DynamicAttirbutesSet and puts them in the
     * scope to be run by the script.
     *
     * @param scope  - scope to populate
     * @param inputs - DynamicAttributeSet that contains the input variable.
     */
    private void convertInputsToScope(Scriptable scope, LinkedHashMap<String, Object> inputs) {

        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            ScriptableObject.putProperty(scope, entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> objectToMap(NativeObject obj) {

        HashMap<String, Object> map = new HashMap<>();

        for (Object id : obj.getIds()) {
            String key;
            Object value;
            if (id instanceof String) {
                key = (String) id;
                value = obj.get(key, obj);
                if (value instanceof NativeArray) {
                    List<Object> list = objectToList((NativeArray) value);
                    map.put(key, list);
                } else {
                    map.put(key, value);
                }

            } else if (id instanceof Integer) {
                key = id.toString();
                value = obj.get(((Integer) id).intValue(), obj);
                if (value instanceof NativeArray) {
                    List<Object> list = objectToList((NativeArray) value);
                    map.put(key, list);
                } else {
                    map.put(key, value);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return map;
    }

    private List<Object> objectToList(NativeArray arr) {

        List<Object> list = new ArrayList<Object>();

        Object[] array = new Object[(int) arr.getLength()];
        for (Object o : arr.getIds()) {
            int index = (Integer) o;
            Object result = arr.get(index, null);
            if (result instanceof Wrapper) {
                result = ((Wrapper) result).unwrap();
            }

            list.add(result);
        }

        return list;
    }


    class EnvistaClassShutter implements ClassShutter {
        private ScriptSecurityPolicy policy;
        private List<String> blackListedClasses;

        public EnvistaClassShutter(ScriptSecurityPolicy policy, List<String> blackListedClasses) {
            this.policy = policy;
            this.blackListedClasses = blackListedClasses;
        }

        public boolean visibleToScripts(String className) {
            for (String packageName : policy.getAllowedPackages().values()) {
                //TODO: Allow everything for now.
                blackListedClasses = new ArrayList<String>();
                if (className.startsWith(packageName) && !blackListedClasses.contains(className)) {

                    return true;
                }
            }
            return false;
        }
    }


    class EnvistaNativeJavaObject extends NativeJavaObject {
        public EnvistaNativeJavaObject(Scriptable scpe, Object javaObject, Class staticType) {
            super(scpe, javaObject, staticType);
        }

        @Override
        public Object get(String name, Scriptable start) {
            if (name.equals("getClass")) {
                return NOT_FOUND;
            }

            return super.get(name, start);
        }
    }

    class EnvistaWrapFactory extends WrapFactory {
        @Override
        public Scriptable wrapAsJavaObject(Context context, Scriptable scpe, Object javaObject, Class staticType) {
            return new EnvistaNativeJavaObject(scpe, javaObject, staticType);
        }
    }

    class EnvistaContextFactory extends ContextFactory {

        @Override
        protected Context makeContext() {
            Context context = super.makeContext();
            context.setWrapFactory(new EnvistaWrapFactory());
            return context;
        }

        @Override
        protected void onContextReleased(Context cx) {
            super.onContextReleased(cx);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context released");
            }

        }
    }


}

