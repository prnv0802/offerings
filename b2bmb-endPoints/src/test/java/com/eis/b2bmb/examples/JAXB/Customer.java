package com.eis.b2bmb.examples.JAXB;

/**
 * User: mingardia
 * Date: 5/13/14
 * Time: 5:24 PM
 */
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlRootElement(namespace="http://enspirecommerce.com")
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer {

    String name;
    int age;
    int id;

    @XmlElement(nillable = true)
    BigDecimal pay;

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }


    public void setAge(int age) {
        this.age = age;
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getPay() {
        return pay;
    }


    public void setPay(BigDecimal pay) {
        this.pay = pay;
    }
}

