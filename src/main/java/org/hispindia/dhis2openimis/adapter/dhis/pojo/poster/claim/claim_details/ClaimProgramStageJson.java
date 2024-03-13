package org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.claim.claim_details;

import org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.DataValue;

import java.util.List;

@SuppressWarnings("rawtypes")
public class ClaimProgramStageJson {
	private List<DataValue> dataValues;
	private String program;
	private String status;
	private String completedDate;
	private String eventDate;
	
	public ClaimProgramStageJson() {
		
	}
	
	public List<DataValue> getDataValues() {
		return dataValues;
	}
	public void setDataValues(List<DataValue> dataValues) {
		this.dataValues = dataValues;
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

	@Override
	public String toString() {
		return "{\"dataValues\":" + dataValues + ", \"program\":" + program + ", \"status\":" + status
				+ ", \"completedDate\":" + completedDate + ", \"eventDate\":" + eventDate + "}";
	}
	
	
}
