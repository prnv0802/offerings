package com.eis.b2bmb.examples.interceptor;


import java.util.LinkedHashMap;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:01 PM
 */
public interface Interceptor {
       public InterceptorContext getCurrentContext();
       public InterceptorContext onBeforeAction(InterceptorContext context, String action) throws InterceptorException;
       public InterceptorContext onAfterAction(InterceptorContext context, String action) throws InterceptorException;
}
