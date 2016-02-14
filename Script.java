/**
 *
 */
package com.eis.core.api.v1.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
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
@XmlType(name = "Script", propOrder = {
		"id",
        "script",
        "type",
        "result",
        "inputs",
        "compensateScript",
        "language",
        "active",
        "scriptTracer",
        "scriptSecurityPolicy",
        "functionType",
        "blacklisted",
        "blacklistReason",
        "scriptTypeId",
        "scriptSecurityPolicyId",
        "runAsId",
        "runAsDefaultDataDomain"
})
public class Script extends BaseModel implements Serializable {

	@XmlElement(required = true)
    protected String id;

    @XmlElement(required = true)
    @NotNull
    protected String script;

    protected String  compensateScript;

    protected String  language;

    @Transient
    @XmlElement(required = true)
    protected ScriptType type;

    @XmlElement(required = true)
    protected String functionType;

    @Transient
    protected LinkedHashMap<String, Object> inputs;

    @Transient
    protected Object result;

    protected boolean active;

    protected boolean blacklisted;

    protected String blacklistReason;

    protected String scriptTracer;

    @Transient
    @XmlElement(required = true)
    protected ScriptSecurityPolicy scriptSecurityPolicy;

    @NotNull
    protected String scriptTypeId;

    protected String scriptSecurityPolicyId;

    /**
     * The userId to run this script as
     */
    // @NotNull - enable this when we are ready to enforce this every where
    protected String runAsId;

    /**
     * The default domain ( should not be set by the user but stored in the script definition
     * as it would be a security issue to allow the user to set the domain in a shared environment
     */
    // @NotNull  - enable this when we are ready to enforce this every where
    protected String runAsDefaultDataDomain;

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

    /**
     * Returns the Script text.
     *
     * @return
     */
	public String getScript() {
		return script;
	}

    /**
     * Sets the Script text.
     *
     * @param script
     */
	public void setScript(String script) {
		this.script = script;
	}

    /**
     * Returns the compensate script.
     *
     * @return
     */
	public String getCompensateScript() {
		return compensateScript;
	}

    /**
     * Sets the compensate script.
     *
     * @param compensateScript
     */
	public void setCompensateScript(String compensateScript) {
		this.compensateScript = compensateScript;
	}

    /**
     * Returns the script language.
     *
     * @return
     */
	public String getLanguage() {
		return language;
	}

    /**
     * Sets the script language.
     *
     * @param language
     */
	public void setLanguage(String language) {
		this.language = language;
	}

    public LinkedHashMap<String,Object> getInputs() {
        if(inputs == null) {
            inputs = new LinkedHashMap<String,Object>();
        }
        return inputs;
    }


    /**
     * Returns an object containing the results.
     *
     * @return
     */
    public Object getResult() {
        return result;
    }

    /**
     * Sets the object containing the result.
     *
     * @param result
     */
    public void setResult(Object result) {
        this.result = result;
    }


    /**
     * Returns the script type.
     *
     * @return
     */
    public ScriptType getType() {
        return type;
    }

    /**
     * Sets the script type.
     * @param type
     */
    public void setType(ScriptType type) {
        this.type = type;
    }

    /**
     * Returns a boolean indicating if this the active script for the script type.
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets a boolean indicating if this the active script for the script type.
     *
     * @return
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns a String containing a trace of the what happened in the
     * script.
     *
     * @return String
     */
    public String getScriptTracer() {
        return scriptTracer;
    }

    /**
     * Sets a String containg a trace of what happens in the script.
     * @param scriptTracer - the tracer
     */
    public void setScriptTracer(String scriptTracer) {
        this.scriptTracer = scriptTracer;
    }

    /**
     * Returns the security policy for this script.
     *
     * @return - the script secuirty policy
     */
    public ScriptSecurityPolicy getScriptSecurityPolicy() {
        return scriptSecurityPolicy;
    }

    /**
     * Sets the security policy for this script.
     *
     * @param - the script security policy
     */
    public void setScriptSecurityPolicy(ScriptSecurityPolicy scriptSecurityPolicy) {
        this.scriptSecurityPolicy = scriptSecurityPolicy;
    }

    /**
     * String identifying the function of the script.
     *
     * @return String identifying function of the script.
     */
    public String getFunctionType() {
        return functionType;
    }

    /**
     * Sets String identifying the function of the script.
     *
     * @param functionType - String identifying function of the script.
     */
    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    /**
     * Boolean which indicates if the script was blacklisted.
     *
     * @return boolean which indicates if script was blacklisted.
     */
    public boolean isBlacklisted() {
        return blacklisted;
    }

    /**
     * Boolean which indicates if the script was blacklisted.
     *
     * @param blacklisted - boolean which indicates if script was blacklisted
     */
    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    /**
     * Returns the reason why the script was black listed.
     *
     * @return - String for the reason why the script was blacklisted
     */
    public String getBlacklistReason() {
        return blacklistReason;
    }

    /**
     * Sets the reason why the script was black listed.
     *
     * @param blacklistReason - String for the reason why the script was blacklisted
     */
    public void setBlacklistReason(String blacklistReason) {
        this.blacklistReason = blacklistReason;
    }

    public String getScriptTypeId() {
        return scriptTypeId;
    }

    public void setScriptTypeId(String scriptTypeId) {
        this.scriptTypeId = scriptTypeId;
    }

    public String getScriptSecurityPolicyId() {
        return scriptSecurityPolicyId;
    }

    public void setScriptSecurityPolicyId(String scriptSecurityPolicyId) {
        this.scriptSecurityPolicyId = scriptSecurityPolicyId;
    }

    public String getRunAsDefaultDataDomain() {

        return runAsDefaultDataDomain;
    }

    public void setRunAsDefaultDataDomain(String runAsDefaultDataDomain) {

        this.runAsDefaultDataDomain = runAsDefaultDataDomain;
    }

    public String getRunAsId() {

        return runAsId;
    }

    public void setRunAsId(String runAsId) {

        this.runAsId = runAsId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Script script1 = (Script) o;

        if (active != script1.active) return false;
        if (blacklisted != script1.blacklisted) return false;
        if (id != null ? !id.equals(script1.id) : script1.id != null) return false;
        if (script != null ? !script.equals(script1.script) : script1.script != null) return false;
        if (compensateScript != null ? !compensateScript.equals(script1.compensateScript) : script1.compensateScript
                != null)
            return false;
        if (language != null ? !language.equals(script1.language) : script1.language != null) return false;
        if (type != null ? !type.equals(script1.type) : script1.type != null) return false;
        if (functionType != null ? !functionType.equals(script1.functionType) : script1.functionType != null)
            return false;
        if (inputs != null ? !inputs.equals(script1.inputs) : script1.inputs != null) return false;
        if (result != null ? !result.equals(script1.result) : script1.result != null) return false;
        if (blacklistReason != null ? !blacklistReason.equals(script1.blacklistReason) : script1.blacklistReason !=
                null)
            return false;
        if (scriptTracer != null ? !scriptTracer.equals(script1.scriptTracer) : script1.scriptTracer != null)
            return false;
        if (scriptSecurityPolicy != null ? !scriptSecurityPolicy.equals(script1.scriptSecurityPolicy) : script1
                .scriptSecurityPolicy != null)
            return false;
        if (scriptTypeId != null ? !scriptTypeId.equals(script1.scriptTypeId) : script1.scriptTypeId != null)
            return false;
        if (scriptSecurityPolicyId != null ? !scriptSecurityPolicyId.equals(script1.scriptSecurityPolicyId) : script1
                .scriptSecurityPolicyId != null)
            return false;
        if (runAsId != null ? !runAsId.equals(script1.runAsId) : script1.runAsId != null) return false;
        return !(runAsDefaultDataDomain != null ? !runAsDefaultDataDomain.equals(script1.runAsDefaultDataDomain) :
                script1
                .runAsDefaultDataDomain != null);

    }

    @Override
    public int hashCode() {

        int result1 = super.hashCode();
        result1 = 31 * result1 + (id != null ? id.hashCode() : 0);
        result1 = 31 * result1 + (script != null ? script.hashCode() : 0);
        result1 = 31 * result1 + (compensateScript != null ? compensateScript.hashCode() : 0);
        result1 = 31 * result1 + (language != null ? language.hashCode() : 0);
        result1 = 31 * result1 + (type != null ? type.hashCode() : 0);
        result1 = 31 * result1 + (functionType != null ? functionType.hashCode() : 0);
        result1 = 31 * result1 + (inputs != null ? inputs.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (active ? 1 : 0);
        result1 = 31 * result1 + (blacklisted ? 1 : 0);
        result1 = 31 * result1 + (blacklistReason != null ? blacklistReason.hashCode() : 0);
        result1 = 31 * result1 + (scriptTracer != null ? scriptTracer.hashCode() : 0);
        result1 = 31 * result1 + (scriptSecurityPolicy != null ? scriptSecurityPolicy.hashCode() : 0);
        result1 = 31 * result1 + (scriptTypeId != null ? scriptTypeId.hashCode() : 0);
        result1 = 31 * result1 + (scriptSecurityPolicyId != null ? scriptSecurityPolicyId.hashCode() : 0);
        result1 = 31 * result1 + (runAsId != null ? runAsId.hashCode() : 0);
        result1 = 31 * result1 + (runAsDefaultDataDomain != null ? runAsDefaultDataDomain.hashCode() : 0);
        return result1;
    }
}
