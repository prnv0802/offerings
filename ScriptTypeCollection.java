/**
 * 
 */
package com.eis.core.api.v1.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a Script Type collections
 * @author faija
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScriptTypeCollection", propOrder = {
        "items"
})
public class ScriptTypeCollection extends Collection {

	@XmlElement(required = true)
    protected List<ScriptType> items;

  /**
     * Gets the value of the items property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the items property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItems().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.eis.core.api.v1.model.Script }
     *
     *
     */

	 public List<ScriptType> getItems() {
	        if (items == null) {
	            items = new ArrayList<ScriptType>();
	        }
	        return this.items;
	    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ScriptTypeCollection))
			return false;
		ScriptTypeCollection other = (ScriptTypeCollection) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}

}
