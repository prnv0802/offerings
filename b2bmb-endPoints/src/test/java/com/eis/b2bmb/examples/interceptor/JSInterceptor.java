package com.eis.b2bmb.examples.interceptor;

import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.Script;
import com.eis.core.common.JSRunner;


import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 5:15 PM
 */
public class JSInterceptor extends BaseInterceptorImpl implements Interceptor {

    protected JSRunner runner = new JSRunner();


    protected Script onBeforeActionScript;
    protected List<DynamicAttribute> beforeParameters;

    protected Script onAfterActionScript;
    protected List<DynamicAttribute> afterParameters;

    public Script getOnBeforeActionScript() {
        return onBeforeActionScript;
    }

    public void setOnBeforeActionScript(Script onBeforeActionScript) {
        this.onBeforeActionScript = onBeforeActionScript;
    }

    public Script getOnAfterActionScript() {
        return onAfterActionScript;
    }

    public void setOnAfterActionScript(Script onAfterActionScript) {
        this.onAfterActionScript = onAfterActionScript;
    }

    public List<DynamicAttribute> getAfterParameters() {
        return afterParameters;
    }

    public void setAfterParameters(List<DynamicAttribute> afterParameters) {
        this.afterParameters = afterParameters;
    }

    @Override
    public InterceptorContext onBeforeAction(InterceptorContext context, String action) throws InterceptorException {
        if (onBeforeActionScript != null)
        {
            onBeforeActionScript.getInputs().put("context", context);
            if (beforeParameters != null)
            {
                for (DynamicAttribute attr : this.beforeParameters)
                {
                    Object obj = context.get(attr.getRefName());
                    if (obj == null && attr.getDefaultValue() != null)
                    {
                        onBeforeActionScript.getInputs().put(attr.getRefName(), attr.getDefaultValue());
                    }
                    else
                    if (obj != null)
                    {
                        onBeforeActionScript.getInputs().put(attr.getRefName(), obj);
                    }
                }
            }



            try {
                runner.runJS(onBeforeActionScript, new ArrayList<String>());
            } catch (B2BNotFoundException | ScriptException | B2BTransactionFailed
                    | B2BNotAuthenticatedException | B2BNotAuthorizedException e) {
                throw new InterceptorException("The script threw an exception", e);
            }
        }

        return context;
    }

    @Override
    public InterceptorContext onAfterAction(InterceptorContext context, String action) throws InterceptorException {
        return null;
    }
}
