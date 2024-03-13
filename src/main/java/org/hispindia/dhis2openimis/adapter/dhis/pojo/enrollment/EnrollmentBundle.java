/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.hispindia.dhis2openimis.adapter.dhis.pojo.enrollment;

import java.util.List;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.option.OptionDhis2;

/**
 *
 * @author Mithilesh Thakur
 */
public class EnrollmentBundle {
    private List<Enrollment> enrollments;

    /**
     * @return the Enrollment
     */
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    /**
     * @param OptionDhis2 the Enrollment to set
     */
    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
    

    @Override
    public String toString() {
            return "EnrollmentBundle [enrollments=" + enrollments + "]";
    }
}
