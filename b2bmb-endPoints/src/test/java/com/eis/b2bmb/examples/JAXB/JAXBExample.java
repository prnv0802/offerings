package com.eis.b2bmb.examples.JAXB;

/**
 * User: mingardia
 * Date: 5/13/14
 * Time: 5:25 PM
 */

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.math.BigDecimal;

public class JAXBExample {
    public static void main(String[] args) {

        Customer customer = new Customer();
        customer.setId(100);
        customer.setName("");
        customer.setAge(29);
        //customer.setPay(new BigDecimal(0));

        try {

            //File file = new File("C:\\file.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            //jaxbMarshaller.marshal(customer, file);
            jaxbMarshaller.marshal(customer, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
}