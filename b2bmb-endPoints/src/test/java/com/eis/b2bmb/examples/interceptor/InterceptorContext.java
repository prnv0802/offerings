package com.eis.b2bmb.examples.interceptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:03 PM
 */
public interface InterceptorContext<K,V>  {
    public V get(Object key);

    public V put(K key, V obj);
}
