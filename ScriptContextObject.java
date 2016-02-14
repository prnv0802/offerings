package com.eis.core.api.v1.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.LinkedHashMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScriptContextObject", propOrder = {
        "type",
        "name",
        "methodName",
        "modelName",
        "serviceName",
        "parameters",
        "parameterName"
})
public class ScriptContextObject implements Serializable {

   protected String type;

   protected String name;

   protected String methodName;

   protected String modelName;

   protected String serviceName;

   protected String parameterName;

   protected LinkedHashMap<String, Object> parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LinkedHashMap<String, Object> getParameters() {

        if (this.parameters == null) {
            parameters = new LinkedHashMap<String, Object>();
        }

        return parameters;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptContextObject that = (ScriptContextObject) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (modelName != null ? !modelName.equals(that.modelName) : that.modelName != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (modelName != null ? modelName.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}
