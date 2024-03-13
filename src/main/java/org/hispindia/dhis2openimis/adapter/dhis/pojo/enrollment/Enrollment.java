/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.hispindia.dhis2openimis.adapter.dhis.pojo.enrollment;

/**
 *
 * @author Mithilesh Thakur
 */
public class Enrollment {
	private String trackedEntityInstance;
        private String enrollment;
	private String program;
	private String status;
	private String orgUnit;
	private String enrollmentDate;
	private String incidentDate;
	
	public Enrollment() {
		
	}
	
	public String getTrackedEntityInstance() {
		return trackedEntityInstance;
	}
	public void setTrackedEntityInstance(String trackedEntityInstance) {
		this.trackedEntityInstance = trackedEntityInstance;
	}

        public String getEnrollment() {
            return enrollment;
        }

        public void setEnrollment(String enrollment) {
            this.enrollment = enrollment;
        }

	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOrgUnit() {
		return orgUnit;
	}
	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}
	public String getEnrollmentDate() {
		return enrollmentDate;
	}
	public void setEnrollmentDate(String enrollmentDate) {
		this.enrollmentDate = enrollmentDate;
	}
	public String getIncidentDate() {
		return incidentDate;
	}
	public void setIncidentDate(String incidentDate) {
		this.incidentDate = incidentDate;
	}

	@Override
	public String toString() {
		return "{\"trackedEntityInstance\":\"" + trackedEntityInstance + "\", \"enrollment\":\"" + enrollment + "\", \"program\":\"" + program
				+ "\", \"status\":\"" + status + "\", \"orgUnit\":\"" + orgUnit + "\", \"enrollmentDate\":\"" + enrollmentDate
				+ "\", \"incidentDate\":\"" + incidentDate + "\"}";
	}
	    
}
