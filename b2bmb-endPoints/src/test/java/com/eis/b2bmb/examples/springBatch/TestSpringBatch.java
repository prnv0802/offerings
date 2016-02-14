package com.eis.b2bmb.examples.springBatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: mingardia
 * Date: 12/12/13
 * Time: 9:01 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
public class TestSpringBatch {


    @Test
    public void testBatch() throws InterruptedException {
        Thread.currentThread().sleep(1000);
    }

}
