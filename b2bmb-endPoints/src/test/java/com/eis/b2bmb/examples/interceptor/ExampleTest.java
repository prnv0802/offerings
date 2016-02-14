package com.eis.b2bmb.examples.interceptor;

import com.eis.core.api.v1.model.Script;
import com.eis.core.common.JSRunner;
import com.eis.spring.util.SpringApplicationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:18 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
public class ExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleTest.class);

    @Autowired
    InterceptorChain interceptorChain;


    @Test
    public void testSimple() {
        LOG.info("--- Simple ---");
        Interceptor interceptor = (Interceptor) SpringApplicationContext.getBean("defaultInterceptor");

        if (interceptor == null) {
            fail("could not find default interceptor in config, check config");
        }

        ExampleMailbox mb = new ExampleMailbox();
        mb.setInterceptor(interceptor);

        mb.addString("Test1");
        mb.removeString("Test1");

    }


    @Test
    public void testChain() {
       LOG.info("--- Chain Example ---");
        Map beans = SpringApplicationContext.getBeansThatAre(Interceptor.class);

        InterceptorChain chain = new InterceptorChain();

        Iterator it = beans.values().iterator();


        while (it.hasNext()) {
            chain.replaceOrAddInterceptor((Interceptor) it.next());
        }

        ExampleMailbox mb = new ExampleMailbox();
        mb.setInterceptor(chain);

        mb.addString("Test1");
        mb.removeString("Test1");


    }

    @Test
    public void testChainConfiguredVaiSpring() {
        LOG.info("--- Autowired Example ---");

        ExampleMailbox mb = new ExampleMailbox();
        mb.setInterceptor(interceptorChain);

        mb.addString("Test1");
        mb.removeString("Test1");
    }


    @Test
    public void testChainWithGuards() {
        LOG.info("--- Guarded Example ---");

        Interceptor i = (Interceptor) SpringApplicationContext.getBean("guardedInterceptorChain");

        i.getCurrentContext().put("stopChain", new Object());

        ExampleMailbox mb = new ExampleMailbox();
        mb.setInterceptor(i);

        mb.addString("Test1");
        mb.removeString("Test1");



    }


    @Test
    public void testPrototypeBean() {
        LOG.info("--- Loading the guard class multiple times ---");

        MyGuard guard1 = (MyGuard) SpringApplicationContext.getBean("myGuard");

        guard1.setSampleExtensionProperty("*.txt");

        MyGuard guard2 = (MyGuard) SpringApplicationContext.getBean("myGuard");

        guard2.setSampleExtensionProperty("*.x12");

        assertNotEquals(guard1.getSampleExtensionProperty(), guard2.getSampleExtensionProperty());


    }


   // @Test
   // waiting on correlationDAO to be removed from JSrunner.
    public void testJavaScriptInterceptor()
    {
       // we would load this from the mongo database
       String exampleScript = "print('!!!!!! Hello There !!!!!')";


       JSInterceptor userDefinedInterceptor = (JSInterceptor) SpringApplicationContext.getBean("jsInterceptor");
       Script userScript = new Script();
       userScript.setScript(exampleScript);

        userDefinedInterceptor.setOnBeforeActionScript(userScript);

        interceptorChain.replaceOrAddInterceptor(userDefinedInterceptor);

        ExampleMailbox mb = new ExampleMailbox();
        mb.setInterceptor(interceptorChain);

        mb.addString("Test1");
        mb.removeString("Test1");


    }

}
