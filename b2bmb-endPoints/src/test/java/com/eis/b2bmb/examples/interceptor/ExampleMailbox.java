package com.eis.b2bmb.examples.interceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:07 PM
 */
public class ExampleMailbox {

    Map<String, String> strings = new HashMap<String, String>();
    Interceptor interceptor;


    public void setInterceptor(Interceptor i)
    {
        this.interceptor = i;
    }

    public String addString(String entry)
    {
        if (interceptor != null)
        {
             InterceptorContext context = interceptor.getCurrentContext();

             String beforeObject = strings.get(entry);

             context.put("beforeObject", beforeObject);
             context.put("afterObject", entry);

            try {
                interceptor.onBeforeAction(context, "exampleMailbox.addString");
            } catch (InterceptorException e) {
                e.printStackTrace();
            }
        }

        String rc = strings.put(entry, entry);

        if (interceptor != null)
        {
            InterceptorContext context = interceptor.getCurrentContext();

            String beforeObject = strings.get(entry);

            context.put("beforeObject", rc);
            context.put("afterObject", entry);

            try {
                interceptor.onAfterAction(context, "exampleMailbox.addString");
            } catch (InterceptorException e) {
                e.printStackTrace();
            }
        }

        return rc;
    }

    public String removeString(String entry)
    {

        if (interceptor != null)
        {
            InterceptorContext context = interceptor.getCurrentContext();

            String beforeObject = strings.get(entry);

            context.put("beforeObject", beforeObject);
            context.put("afterObject", entry);

            try {
                interceptor.onBeforeAction(context, "exampleMailbox.deleteString");
            } catch (InterceptorException e) {
                e.printStackTrace();
            }
        }

        String rc =  strings.remove(entry);

        if (interceptor != null)
        {
            InterceptorContext context = interceptor.getCurrentContext();

            String beforeObject = strings.get(entry);

            context.put("beforeObject", rc);
            context.put("afterObject", entry);

            try {
                interceptor.onAfterAction(context, "exampleMailbox.deleteString");
            } catch (InterceptorException e) {
                e.printStackTrace();
            }
        }


        return rc;
    }
}
