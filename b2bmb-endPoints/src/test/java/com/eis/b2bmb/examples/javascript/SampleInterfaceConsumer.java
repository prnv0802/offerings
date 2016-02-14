package com.eis.b2bmb.examples.javascript;

import com.eis.core.api.v1.model.Site;

import java.util.List;

/**
 * User: mingardia
 * Date: 12/19/13
 * Time: 8:45 AM
 */
public class SampleInterfaceConsumer {


    protected SampleInterface sample;

    public void setSampleInterface(SampleInterface sample)
    {
        this.sample = sample;
    }

    public Site getSite(List<Site> sites)
    {
        return sample.chooseSite(sites);
    }

    public SampleInterface getSampleInterface() {
        return sample;
    }
}
