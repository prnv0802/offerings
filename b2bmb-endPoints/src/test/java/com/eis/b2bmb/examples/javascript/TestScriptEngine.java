package com.eis.b2bmb.examples.javascript;

import com.eis.core.api.v1.model.Site;
import org.junit.Test;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: mingardia
 * Date: 12/16/13
 * Time: 3:35 PM
 */
public class TestScriptEngine {


    private static final Logger LOG = LoggerFactory.getLogger(TestScriptEngine.class);

    //@Test
    public void testEngine() throws ScriptException {

        if (LOG.isInfoEnabled()) {
            LOG.info("--- Executing testEngine ---");
        }


        ClassShutter shutter = new ClassShutter() {
            @Override
            public boolean visibleToScripts(String s) {

                System.out.println("Trying to access java class:" + s);
                return true;

            }
        };

        if (!ContextFactory.hasExplicitGlobal()) {
            ContextFactory.initGlobal(new SandboxContextFactory());

        }


        try {
            Context ctx = ContextFactory.getGlobal().enterContext();

            ctx.setClassShutter(shutter);

            ScriptableObject scope = ctx.initStandardObjects();

            Object jsOut = Context.javaToJS(System.out, scope);

            ScriptableObject.putProperty(scope, "xout", jsOut);

            String args = "";

            Object result = ctx.evaluateString(scope, "xout.println('Hello 123'); ", "<cmd>", 1, null);

        } finally {
            Context.exit();
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("--- Done Executing testEngine ---");
        }

    }

    @Test
    public void testInterface() throws ScriptException {

        if (LOG.isInfoEnabled()) {
            LOG.info("--- Executing testInterface ---");
        }

        ClassShutter shutter = new ClassShutter() {
            @Override
            public boolean visibleToScripts(String s) {

                System.out.println("Trying to access java class:" + s);
                return true;

            }
        };

        if (!ContextFactory.hasExplicitGlobal()) {
            ContextFactory.initGlobal(new SandboxContextFactory());

        }


        try {
            Context ctx = ContextFactory.getGlobal().enterContext();

            ctx.setClassShutter(shutter);

            ScriptableObject scope = ctx.initStandardObjects();

            SampleInterfaceConsumer consumer = new SampleInterfaceConsumer();


            Object jsOut = Context.javaToJS(System.out, scope);
            Object jsConsumer = Context.javaToJS(consumer, scope);

            ScriptableObject.putProperty(scope, "out", jsOut);
            ScriptableObject.putProperty(scope, "consumer", jsConsumer);


            String args = "";

            //String script = "var obj = {chooseSite : function(listofSites) { return listofSites.get(0); }}; var iobj = new com.eis.b2bmb.examples.javascript.SampleInterface(obj);  var site = new com.eis.ssit.api.v1.model.Site();  site.setRefName('Test'); var sites=[site]; var returned = iobj.chooseSite(sites); out.println(returned.refName); consumer.sampleInterface = iobj;";

            String script = "var obj = {chooseSite : function(listofSites) { return listofSites.get(0); }}; var iobj = new com.eis.b2bmb.examples.javascript.SampleInterface(obj);  consumer.sampleInterface = iobj;";

            Object result = ctx.evaluateString(scope, script, "<cmd>", 1, null);

            if (consumer.getSampleInterface() != null)
            {
                LOG.info("It worked !!!");
            }

            Site testSite = new Site();
            testSite.setRefName("fromJavaSite");
            List<Site> sites = new ArrayList<>();

            sites.add(testSite);

            Site returnedSite = consumer.getSite(sites);

            LOG.info("Returned Site RefName:" + returnedSite.getRefName());



        } finally {
            Context.exit();
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("--- Done Executing testInterface ---");
        }
    }
}
