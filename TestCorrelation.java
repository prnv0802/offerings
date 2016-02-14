package com.eis.b2bmb.api.v1.dao.test;

import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.Correlation;
import com.eis.core.api.v1.model.CorrelationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.*;

/**
 * Test the Correlation DAO
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/envisionDAOContext.xml"})
public class TestCorrelation {

    @Autowired
    CorrelationDAO correlationDAO;

    @Test
    public void testCreateCorrelationModel() throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        String[] refNames = {"MyOneToOneVar", "MyOneToManyVar", "MyManyToManyVar", "MyManyToOneVar",
                "MyManyToManyVar2"};
        String dataDomain = "com.mycompanyxyz";
        // delete correlations to start the test
        deleteCorrelations(refNames, dataDomain);

        // one to one correlation test
        /*
         Tests JSON that will look like:
        {"type" : "OneToOne",
         "key" :"CORP",
         "value": "Corporation",
         "refName" : "MyOneToOneVar2",
         "dataDomain" : "com.mycompanyxyz"
         */
        Correlation<String, String> oneToOneVar = new Correlation<String, String>();
        oneToOneVar.setType(CorrelationType.OneToOne);

        oneToOneVar.setRefName("MyOneToOneVar");
        oneToOneVar.setDataDomain(dataDomain);

        oneToOneVar.setKey("CORP");
        oneToOneVar.setValue("Corporation");
        correlationDAO.save(oneToOneVar);

        Correlation<List<String>, String> dbOneToOneVar = correlationDAO.getById(oneToOneVar.getId());

        assertNotNull(dbOneToOneVar);
        assertNotNull(dbOneToOneVar.getId());
        assertEquals("CORP", dbOneToOneVar.getKey());

        oneToOneVar.setValue("Business");
        correlationDAO.save(oneToOneVar);

        // test update
        dbOneToOneVar = correlationDAO.getById(oneToOneVar.getId());
        assertEquals("Business", dbOneToOneVar.getValue());
        assertEquals("Business", dbOneToOneVar.getValuesForKey("CORP"));

        // one to many correlation test
        /*
        { "type" : "OneToMany",
          "key" :"States",
          "value": ["NJ", "NY","GA"],
          "refName" : "MyOneToManyVar",
          "dataDomain" : "com.mycompanyxyz"}
        */
        Correlation<String, List<String>> oneToNVar = new Correlation<String, List<String>>();

        oneToNVar.setType(CorrelationType.OneToMany);

        oneToNVar.setRefName("MyOneToManyVar");
        oneToNVar.setDataDomain(dataDomain);

        List<String> states = Arrays.asList(new String[]{"NJ", "NY", "GA"});
        oneToNVar.setKey("States");
        oneToNVar.setValue(states);
        correlationDAO.save(oneToNVar);

        Correlation<String, List<String>> dbOneToNVar = correlationDAO.getById(oneToNVar.getId());

        assertNotNull(dbOneToNVar);
        assertNotNull(dbOneToNVar.getId());
        assertEquals(3, dbOneToNVar.getValue().size());
        List<String> dbStates = dbOneToNVar.getValue();
        assertEquals("NJ", dbStates.get(0));
        assertEquals("NY", dbStates.get(1));
        assertEquals("GA", dbStates.get(2));
        List stateValues = (List) dbOneToNVar.getValuesForKey("States");
        assertEquals(3, stateValues.size());


        // many to many correlation test
        /*
        {"type" : "ManyToMany",
         "key" : [["GREEN","BLUE", "RED"],
                 ["TRIANGLE","SQUARE", "CIRCLE"]
                ],
         "value" : [["TRIANGLE", "SQUARE", "CIRCLE"],
                    ["GREEN","BLUE", "RED"]
                   ],
          \"refName" : "MyManyToManyVar2",
         "dataDomain" : "com.mycompanyxyz"
         }
         */
        Correlation<List<List<String>>, List<List<String>>> nToNVar = new Correlation<List<List<String>>,
                List<List<String>>>();
        List<String> colors = Arrays.asList(new String[]{"GREEN", "BLUE", "RED"});
        List<String> shapes = Arrays.asList(new String[]{"TRIANGLE", "SQUARE", "CIRCLE"});
        nToNVar.setType(CorrelationType.ManyToMany);

        nToNVar.setRefName("MyManyToManyVar");
        nToNVar.setDataDomain(dataDomain);

        List list1 = new ArrayList();
        List list2 = new ArrayList();
        list1.add(colors);
        list2.add(shapes);
        nToNVar.setKey(list1);
        nToNVar.setValue(list2);
        correlationDAO.save(nToNVar);

        Correlation<List<List<String>>, List<List<String>>> dbNToNVar = correlationDAO.getById(nToNVar.getId());

        assertNotNull(dbNToNVar);
        assertNotNull(dbNToNVar.getId());

        List shapesValues = (List) dbNToNVar.getValuesForKey("GREEN");
        assertEquals(3, shapesValues.size());

        List<List<String>> dbColors = dbNToNVar.getKey();
        List<List<String>> dbShapes = dbNToNVar.getValue();
        assertEquals(3, dbColors.get(0).size());
        assertEquals(3, dbShapes.get(0).size());
        assertEquals("GREEN", dbColors.get(0).get(0));
        assertEquals("BLUE", dbColors.get(0).get(1));
        assertEquals("RED", dbColors.get(0).get(2));

        assertEquals("TRIANGLE", dbShapes.get(0).get(0));
        assertEquals("SQUARE", dbShapes.get(0).get(1));
        assertEquals("CIRCLE", dbShapes.get(0).get(2));


        Correlation<List<List<String>>, List<List<String>>> nToNVar2 = new Correlation<List<List<String>>,
                List<List<String>>>();

        list1 = new ArrayList();
        list2 = new ArrayList();
        list1.add(colors);
        list1.add(shapes);
        list2.add(shapes);
        list2.add(colors);
        nToNVar2.setType(CorrelationType.ManyToMany);

        nToNVar2.setRefName("MyManyToManyVar2");
        nToNVar2.setDataDomain(dataDomain);

        nToNVar2.setKey(list1);
        nToNVar2.setValue(list2);
        correlationDAO.save(nToNVar2);

        Correlation<String, List<String>> dbNToNVar2 = correlationDAO.getById(nToNVar2.getId());

        assertNotNull(dbNToNVar2);
        assertNotNull(dbNToNVar2.getId());

        List colorValues = (List) dbNToNVar2.getValuesForKey("CIRCLE");
        assertEquals(3, colorValues.size());

        colorValues = (List) dbNToNVar2.getValuesForKey("RECTANGLE");
        assertNull(colorValues);

        // Test a delete
        correlationDAO.delete(nToNVar2);
        Correlation<String, List<String>> dbNToNVar3 = correlationDAO.getByRefName("MyManyToManyVar2", dataDomain);
        assertNull(dbNToNVar3);

        // many to one correlation test
        // many to many correlation test
        /*
        {"type" : "ManyToOne",
         "key" : "DOG",
         "value" : ["LAB", "POODLE","PIT BULL"],
         "refName" : "MyManyToOneVar",
         "dataDomain" : "com.mycompanyxyz"
         }
         */
        Correlation<List<String>, String> nToOneVar = new Correlation<List<String>, String>();
        List<String> dogs = Arrays.asList(new String[]{"LAB", "POODLE", "PIT BULL"});
        nToOneVar.setType(CorrelationType.ManyToOne);

        nToOneVar.setRefName("MyManyToOneVar");
        nToOneVar.setDataDomain(dataDomain);

        nToOneVar.setKey(dogs);
        nToOneVar.setValue("DOG");
        correlationDAO.save(nToOneVar);

        Correlation<String, List<String>> dbNToOneVar = correlationDAO.getById(nToOneVar.getId());

        assertNotNull(dbNToOneVar);
        assertNotNull(dbNToOneVar.getId());

        String dog = (String) dbNToOneVar.getValuesForKey("LAB");
        assertEquals("DOG", dog);
        dog = (String) dbNToOneVar.getValuesForKey("CAT");
        assertNull(dog);

        List<String> dataDomains = new ArrayList<>();
        dataDomains.add(dataDomain);
        List<Correlation> items = correlationDAO.getList(0, 10, null, dataDomains);

        assertTrue(items.size() > 1);


    }

    private void deleteCorrelations(String[] refNames, String dataDomain) throws B2BTransactionFailed,
            B2BNotFoundException {
        for (String refName : refNames) {
            Correlation correlation = correlationDAO.getByRefName(refName, dataDomain);
            if (correlation != null) {
                correlationDAO.delete(correlation);
            }
        }
    }


    @Test
    public void testMultiDomainUnique() throws B2BNotFoundException, B2BTransactionFailed, ValidationException {
        Correlation<String, String> corr1 = new Correlation<String, String>();
        corr1.setType(CorrelationType.OneToOne);
        corr1.setKey("test");
        corr1.setValue("test");
        corr1.setRefName("test");
        corr1.setDataDomain("com.mycompanyxyz");
        corr1 = correlationDAO.save(corr1);
        assertNotNull(corr1.getId());

        Correlation<String, String> corr2 = new Correlation<String, String>();
        corr2.setType(CorrelationType.OneToOne);
        corr2.setKey("test");
        corr2.setValue("test");
        corr2.setRefName("test");
        corr2.setDataDomain("com.mycompanyabc");
        corr2 = correlationDAO.save(corr2);
        assertNotNull(corr2.getId());

        //now update corr2 to add other datadomain
        corr2.getDataDomains().add("com.mycompanyxyz");
        try {
            correlationDAO.save(corr2);
            fail("should have exception");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }

        //try w/ a new one too
        Correlation<String, String> corr3 = new Correlation<String, String>();
        corr3.setType(CorrelationType.OneToOne);
        corr3.setKey("test");
        corr3.setValue("test");
        corr3.setRefName("test");
        corr3.setDataDomain("com.mycompanyabc");
        corr3.getDataDomains().add("com.mycompanyxyz");
        corr3.getDataDomains().add("com.mycompanydef");
        try {
            corr3 = correlationDAO.save(corr3);
            fail("should have exception");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }

        correlationDAO.delete(corr1);
        correlationDAO.delete(corr2);

    }

}

