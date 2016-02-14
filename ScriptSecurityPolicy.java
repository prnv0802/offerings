package com.eis.core.api.v1.model;


import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 *  A  model class for Scripts.
 * @author faija
 *
 */
@Document
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScriptSecurityPolicy", propOrder = {
        "id",
        "allowedPackages",
        "allowedClasses",
        "allowedMethods",
        "permissionsNeeded"
})
public class ScriptSecurityPolicy extends BaseModel implements Serializable{

    @XmlElement(required = true)
    protected String id;

    LinkedHashMap<String, String> allowedPackages;

    LinkedHashMap<String, String> allowedClasses;

    LinkedHashMap<String, String> allowedMethods;

    LinkedHashMap<String, String> permissionsNeeded;


    /**
     * Returns unique id.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id.
     *
     * @return
     */
    public void setId(String id) {
        this.id = id;
    }

    public LinkedHashMap<String, String> getPermissionsNeeded() {
        if (permissionsNeeded == null) {
            permissionsNeeded = new LinkedHashMap<String, String>();
        }

        return permissionsNeeded;
    }

    public LinkedHashMap<String, String> getAllowedPackages() {
        if (allowedPackages == null) {
            allowedPackages = new LinkedHashMap<String, String>();
        }

        return allowedPackages;
    }


    public LinkedHashMap<String, String> getAllowedClasses() {
        if (allowedClasses == null) {
            allowedClasses = new LinkedHashMap<String, String>();
        }

        return allowedClasses;
    }

    public LinkedHashMap<String, String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new LinkedHashMap<String, String>();
        }

        return allowedMethods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptSecurityPolicy that = (ScriptSecurityPolicy) o;

        if (allowedClasses != null ? !allowedClasses.equals(that.allowedClasses) : that.allowedClasses != null)
            return false;
        if (allowedMethods != null ? !allowedMethods.equals(that.allowedMethods) : that.allowedMethods != null)
            return false;
        if (allowedPackages != null ? !allowedPackages.equals(that.allowedPackages) : that.allowedPackages != null)
            return false;
        if (permissionsNeeded != null ? !permissionsNeeded.equals(that.permissionsNeeded) : that.permissionsNeeded != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (allowedPackages != null ? allowedPackages.hashCode() : 0);
        result = 31 * result + (allowedClasses != null ? allowedClasses.hashCode() : 0);
        result = 31 * result + (allowedMethods != null ? allowedMethods.hashCode() : 0);
        result = 31 * result + (permissionsNeeded != null ? permissionsNeeded.hashCode() : 0);
        return result;
    }
}
