/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.hispindia.dhis2openimis.adapter.dhis.pojo.poster;

import java.util.List;

/**
 *
 * @author Mithilesh Thakur
 */
@SuppressWarnings("rawtypes")
public class EventRequestBody {
	private String orgUnit;
	private String trackedEntityInstance;
	private String program;
	private String programStage;
	private String enrollment;
	private String status;
        private String eventDate;
        private String completedDate;
	private String dueDate;
	private List<DataValue> dataValues;

	public EventRequestBody() {

	}

        public String getOrgUnit() {
            return orgUnit;
        }

        public void setOrgUnit(String orgUnit) {
            this.orgUnit = orgUnit;
        }

        public String getTrackedEntityInstance() {
            return trackedEntityInstance;
        }

        public void setTrackedEntityInstance(String trackedEntityInstance) {
            this.trackedEntityInstance = trackedEntityInstance;
        }

        public String getProgram() {
            return program;
        }

        public void setProgram(String program) {
            this.program = program;
        }

        public String getProgramStage() {
            return programStage;
        }

        public void setProgramStage(String programStage) {
            this.programStage = programStage;
        }

        public String getEnrollment() {
            return enrollment;
        }

        public void setEnrollment(String enrollment) {
            this.enrollment = enrollment;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCompletedDate() {
            return completedDate;
        }

        public void setCompletedDate(String completedDate) {
            this.completedDate = completedDate;
        }

        public String getEventDate() {
            return eventDate;
        }

        public void setEventDate(String eventDate) {
            this.eventDate = eventDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public List<DataValue> getDataValues() {
            return dataValues;
        }

        public void setDataValues(List<DataValue> dataValues) {
            this.dataValues = dataValues;
        }

	@Override
	public String toString() {
		return "{\"orgUnit\":\"" + orgUnit + "\", \"trackedEntityInstance\":\"" + trackedEntityInstance 
                        + "\", \"program\":\"" + program +  "\", \"programStage\":\"" + programStage + "\", \"enrollment\":\"" + enrollment 
                        + "\", \"status\":\"" + status + "\", \"completedDate\":\"" + completedDate + "\", \"eventDate\":\"" + eventDate 
                        + "\", \"dueDate\":\"" + dueDate + "\", \"dataValues\":" + dataValues + "}";
        }

}
