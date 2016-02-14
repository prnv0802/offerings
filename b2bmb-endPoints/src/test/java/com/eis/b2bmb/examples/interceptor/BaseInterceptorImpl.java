package com.eis.b2bmb.examples.interceptor;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:52 PM
 */
public class BaseInterceptorImpl {




    public InterceptorContext getCurrentContext() {
        return new QuickAndDirtyContext();
    }

}
