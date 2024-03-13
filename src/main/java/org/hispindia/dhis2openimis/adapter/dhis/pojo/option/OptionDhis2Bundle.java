/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hispindia.dhis2openimis.adapter.dhis.pojo.option;

import java.util.List;

/**
 *
 * @author Mithilesh Thakur
 */
public class OptionDhis2Bundle {
    

    private List<OptionDhis2> options;

    public OptionDhis2Bundle() {

    }
    /**
     * @return the OptionDhis2
     */
    public List<OptionDhis2> getOptions() {
        return options;
    }

    /**
     * @param OptionDhis2 the OptionDhis2 to set
     */
    public void setOptions(List<OptionDhis2> options) {
        this.options = options;
    }

    @Override
    public String toString() {
            return "OptionDhis2Bundle [options=" + options + "]";
    }



	    
}
