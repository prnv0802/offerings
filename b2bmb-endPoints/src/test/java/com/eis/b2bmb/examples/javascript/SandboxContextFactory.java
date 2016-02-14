package com.eis.b2bmb.examples.javascript;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class SandboxContextFactory extends ContextFactory {
    @Override
    protected Context makeContext() {
        Context cx = super.makeContext();
        cx.setWrapFactory(new SandboxWrapFactory());
        return cx;
    }
}