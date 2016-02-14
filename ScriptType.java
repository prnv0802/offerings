package com.eis.core.api.v1.model;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
@XmlType(name = "ScriptType", propOrder = {
        "id",
        "inputs",
        "outputs",
        "scriptContextObjects"
})
public class ScriptType extends BaseModel implements Serializable {

    protected String id;

    protected DynamicAttributeSet inputs;

    protected DynamicAttributeSet outputs;

    protected LinkedHashMap<String, ScriptContextObject> scriptContextObjects;

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

    public DynamicAttributeSet getInputs() {
        return inputs;
    }

    public void setInputs(DynamicAttributeSet inputs) {
        this.inputs = inputs;
    }

    public DynamicAttributeSet getOutputs() {
        return outputs;
    }

    public void setOutputs(DynamicAttributeSet outputs) {
        this.outputs = outputs;
    }

    public LinkedHashMap<String, ScriptContextObject> getScriptContextObjects() {
        if( scriptContextObjects == null )  {
            scriptContextObjects = new LinkedHashMap<String, ScriptContextObject>();
        }

        return scriptContextObjects;
    }









    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ScriptType that = (ScriptType) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (inputs != null ? !inputs.equals(that.inputs) : that.inputs != null) return false;
        if (outputs != null ? !outputs.equals(that.outputs) : that.outputs != null) return false;
        if (scriptContextObjects != null ? !scriptContextObjects.equals(that.scriptContextObjects) : that.scriptContextObjects != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (inputs != null ? inputs.hashCode() : 0);
        result = 31 * result + (outputs != null ? outputs.hashCode() : 0);
        result = 31 * result + (scriptContextObjects != null ? scriptContextObjects.hashCode() : 0);
        return result;
    }
}
