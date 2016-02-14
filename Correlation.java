package com.eis.core.api.v1.model;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.bind.annotation.*;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: tcostanzo
 * Date: 12/2/13
 * Time: 10:33 AM
 *
 */
@Document
@XmlRootElement
@CompoundIndexes({@CompoundIndex(name = "refName_idx", def="{'refName': 1, 'dataDomain': 1}", unique=true)})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Correlation", propOrder = {
        "id",
        "key",
        "keyTypeAsString",
        "value",
        "valueTypeAsString",
        "type",
        "defaultValue",
        "tags"
})
public class Correlation<K,V> extends BaseModel {
    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected CorrelationType type;
    @XmlElement(required = true)
    protected K key;

    @XmlElement(required = true)
    protected String keyTypeAsString;

    @XmlElement(required = true)
    protected String valueTypeAsString;

    @XmlElement(required = true)
    protected V value;


    @XmlElement
    protected V defaultValue;

    @XmlElement()
    protected List<String> tags;


    public Correlation()
    {

    }


    /**
     * The key value
     * @return the key value for a 1: 1 mapping
     */
    public K getKey()
    {
        return key;
    }

    /**
     * For a 1 to 1 mapping this is the value
     * @return   The Value
     */
    public V getValue()
    {
        return value;
    }

    /**
     * Gets the id
     *
     * @return String - object id
     */
    @Override
    public String getId() {
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets id.
     *
     * @param id - id of the object
     * @return
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the CorrelationType which identifies the relationship type.
     * @return
     */
    public CorrelationType getType() {
        return type;
    }

    /**
     * Sets the CorrelationType for the Correlation.
     *
     * @param type - CorrelationType
     */
    public void setType(CorrelationType type) {
        this.type = type;
    }

    /**
     * Sets key of the Correlation for a one to one relationship.
     *
     * @param key  - key for a one to one relationship
     */
    public void setKey(K key) {
        keyTypeAsString = key.getClass().getSimpleName();

        this.key = key;

    }

    /**
     * Sets the value of Correlation for a one to one relationship.
     *
     * @param value  - value of the one to one relationship.
     */
    public void setValue(V value) {
        valueTypeAsString = value.getClass().getSimpleName();
        this.value = value;

    }

    /**
     * Finds the values for a given key.
     * @param k
     * @return The values for a given key - either a String, List<String> or null
     */
    public Object getValuesForKey(Object k){
      if(type.equals(CorrelationType.OneToOne)){
         if(k.equals(key)){
             return value;
         } else {
             return null;
         }
      } else if (type.equals(CorrelationType.OneToMany)) {
          if(k.equals(key)){
              return value;
          } else {
              return null;
          }
      } else if (type.equals(CorrelationType.ManyToMany)) {
          int keyNum = findKeyInList(k, key);
          List values = (List) value;
          if(keyNum != -1) {
            return values.get(keyNum);
          } else {
             return null;
          }
      }  else {
          List keys = (List) key;
          if(keys.contains(k)) {
              return (String) value;
          } else {
              return null;
          }
      }


    }

    private int findKeyInList(Object k, K key) {
        int i = 0;
        if(key instanceof  List) {
            List tempList = (List) key;
            for(Object object : tempList) {
                if(object instanceof List) {
                    List tempList1 = (List) object;
                    if(tempList1.contains(k)) {
                        return i;
                    }
                    i++;
                } else {
                    if(object.equals(k)) {
                        return i;
                    }
                    i++;
                }
            }
        }

        return -1;
    }

    /**
     * get the keytype, note the "K" of this value should match but we are allowing this mainly for the
     * user interface to display what the type is to the user using a convention.
     * @return the string to display to the user interface
     */
    public String getKeyTypeAsString() {
        return keyTypeAsString;
    }

    /**
     * sets the key type string for the ui
     * @param keyTypeAsString - the string to display
     */
    public void setKeyTypeAsString(String keyTypeAsString) {
        this.keyTypeAsString = keyTypeAsString;
    }

    /**
     *  Get teh value as type
     * @return the value
     */
    public String getValueTypeAsString() {
        return valueTypeAsString;
    }

    public void setValueTypeAsString(String valueTypeAsString) {
        this.valueTypeAsString = valueTypeAsString;
    }

    public V getDefaultValue() {

        return defaultValue;
    }

    public void setDefaultValue(V defaultValue) {

        this.defaultValue = defaultValue;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Correlation<?, ?> that = (Correlation<?, ?>) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != that.type) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (keyTypeAsString != null ? !keyTypeAsString.equals(that.keyTypeAsString) : that.keyTypeAsString != null)
            return false;
        if (valueTypeAsString != null ? !valueTypeAsString.equals(that.valueTypeAsString) : that.valueTypeAsString !=
                null)
            return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        return tags != null ? tags.equals(that.tags) : that.tags == null;

    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (keyTypeAsString != null ? keyTypeAsString.hashCode() : 0);
        result = 31 * result + (valueTypeAsString != null ? valueTypeAsString.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {

        return "Correlation{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", key=" + key +
                ", keyTypeAsString='" + keyTypeAsString + '\'' +
                ", valueTypeAsString='" + valueTypeAsString + '\'' +
                ", value=" + value +
                ", defaultValue=" + defaultValue +
                ", tags=" + tags +
                '}';
    }
}

