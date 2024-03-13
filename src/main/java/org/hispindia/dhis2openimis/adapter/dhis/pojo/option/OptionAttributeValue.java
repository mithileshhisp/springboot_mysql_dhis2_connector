/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hispindia.dhis2openimis.adapter.dhis.pojo.option;


/**
 *
 * @author Mithilesh Thakur
 */
public class OptionAttributeValue {
    
    private String value;
    private OptionAttribute attribute;

    public OptionAttributeValue() {

    }
    public OptionAttributeValue(OptionAttribute attribute, String value) {
            this.attribute = attribute;
            this.value = value;
    }   

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the attribute
     */
    public OptionAttribute getAttribute() {
        return attribute;
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(OptionAttribute attribute) {
        this.attribute = attribute;
    }
 
     @Override
    public String toString() {
            return "OptionAttributeValue [value=" + value + ", attribute=" + attribute + "]";
    }   
    
}
