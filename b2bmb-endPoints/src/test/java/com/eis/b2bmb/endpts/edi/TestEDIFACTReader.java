package com.eis.b2bmb.endpts.edi;

import com.eis.base.test.TestBase;
import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.license.LicenseConstants;
import inedi.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

/**
 * User: mingardia
 * Date: 10/26/13
 * Time: 8:15 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
public class TestEDIFACTReader extends TestBase{

    String interchangeHeader = null;
    String groupHeader = null;

    @Autowired
    CorrelationDAO correlationDAO;

    @Test
    public void testEDIReader() throws InEDIException, TooManyListenersException, B2BNotFoundException,
            B2BTransactionFailed, B2BNotAuthenticatedException, B2BNotAuthorizedException  {
        final Edireader edireader1 = new Edireader();
        edireader1.setRuntimeLicense(LicenseConstants.getEdiLicenseString());



        edireader1.addEdireaderEventListener(new DefaultEdireaderEventListener(){

            List<EdireaderWarningEvent> warnings = new ArrayList<EdireaderWarningEvent>();

            public void startInterchange(EdireaderStartInterchangeEvent e){
                System.out.println("StartInterchange: " + " Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:" + e.fullSegment);
                interchangeHeader = e.fullSegment;

                try {
                    String componentDelimiter = edireader1.config("ComponentDelimiter");
                    String elementDelimiter = edireader1.config("ElementDelimiter");
                    String segmentDelimiter = edireader1.config("SegmentDelimiter");
                    String releaseCharacter = edireader1.config("ReleaseChar");

                    System.out.println("component delimeter:" + componentDelimiter);
                    System.out.println("segment delimeter:" + segmentDelimiter);
                    System.out.println("element delimeter:" + elementDelimiter);
                    System.out.println("releaseCharcter:" + releaseCharacter);

                } catch (InEDIException e1) {
                    e1.printStackTrace();
                }

            }
            public void endInterchange(EdireaderEndInterchangeEvent e){
                System.out.println("EndInterchange: " + " Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:" + e.fullSegment);
            }

            public void startFunctionalGroup(EdireaderStartFunctionalGroupEvent e){
                System.out.println("StartFunctionalGroup: " + " Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:" + e.fullSegment);
                if(groupHeader == null) {
                    groupHeader = e.fullSegment;
                }
            }
            public void endFunctionalGroup(EdireaderEndFunctionalGroupEvent e){
                System.out.println("EndFunctionalGroup: " + "Tag:" + e.tag + " Control Number:" + e.controlNumber + " Count:" + e.count + " FullSegment:" + e.fullSegment);
            }

            public void startTransaction(EdireaderStartTransactionEvent e){
                System.out.println("StartTransaction: " + "Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:" + e.fullSegment);
            }
            public void endTransaction(EdireaderEndTransactionEvent e){
                System.out.println("EndTransaction: " + "Tag:" + e.tag + " Control Number:" + e.controlNumber + " Count:" + e.count + " FullSegment:" + e.fullSegment);
            }

            public void startLoop(EdireaderStartLoopEvent e){
                System.out.println("StartLoop: " + e.name);
            }
            public void endLoop(EdireaderEndLoopEvent e){
                System.out.println("EndLoop");
            }

            public void error(EdireaderErrorEvent e){
                System.out.println("ERROR: " + e.errorCode + ":" + e.description);
            }

            public void resolveSchema(EdireaderResolveSchemaEvent e){
                System.out.println("ResolveSchema: " + e.transactionCode );

            }
            public void segment(EdireaderSegmentEvent e){
                System.out.println("Segment: " + e.name + "|L:" + e.loop + "|t:" + e.tag + "|F:" + e.fullSegment);
            }



            public void warning(EdireaderWarningEvent e){
                System.out.println("WARNING: " + e.warnCode + ": " + e.message);
                warnings.add(e);
            }

            public List<EdireaderWarningEvent> getWarnings() {
                return warnings;
            }
        });

        try
        {
            edireader1.config("Encoding=iso-8859-1");
            edireader1.config("CrossFieldValidationEnabled=True");
            edireader1.setEDIStandard(Edireader.esEDIFACT);
            edireader1.setSchemaFormat(Edireader.schemaSEF);
            //edireader1.loadSchema("./sampleData/edifactTest/test_sefFile.sef");
            //edireader1.loadSchema("./sampleData/edifactTest/test_96A_ORDERS.xsd");
            edireader1.parseFile("./sampleData/sampleData/AmazonEU_ORDER.edi");

           // System.out.println(edireader1.config("converttoxml=./sampleData/BaseLine/Seph_850_Single1.x12"));

            //ystem.out.println("-- Start -- ");
           // edireader1.setXPath("/ISA/GS/ST/BEG[1]");

            EDIElementList list = edireader1.getXElements();

            for (EDIElement element : list)
            {
                System.out.println(element.getName());
                for (int r = 0; r> element.getComponentCount(); r++)
                {
                    element.setComponentIndex(r);
                    System.out.println("   " + element.getComponentName());
                }
            }

            System.out.println("-- Out --");


            serverSideLogin();

        }
        finally {
            logout();
        }
    }
}
