/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.hispindia.dhis2openimis.adapter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hispindia.dhis2openimis.adapter.dhis.cache.program.ProgramStageCache;

import org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.enrollment.EnrollmentRequestBody;
import org.hispindia.dhis2openimis.adapter.dhis.cache.org_unit.OrganisationUnitCacheService;
import org.hispindia.dhis2openimis.adapter.dhis.cache.program.ProgramCache;

import org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.Attribute;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.TrackedEntityRequest;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.EventRequestBody;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.poster.DataValue;
import org.hispindia.dhis2openimis.adapter.dhis.cache.TrackedEntityTypeCache;
import org.hispindia.dhis2openimis.adapter.dhis.cache.attribute.TrackedEntityAttributeCache;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.option.OptionDhis2Bundle;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.option.OptionDhis2;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.option.OptionAttributeValue;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitAttribute;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitAttributeRequest;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitAttributeValue;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitAttributeValueRequest;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitDhis2;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitDhis2Request;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitParentRequest;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitPostResponse;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.organisation.OrganisationUnitsDhis2Bundle;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.post_response.TrackedEntityPostResponse;
import org.hispindia.dhis2openimis.adapter.dhis.util.CreateEventDataPojo;
import org.hispindia.dhis2openimis.adapter.util.exception.InternalException;
import org.hispindia.dhis2openimis.adapter.util.exception.ObjectNotFoundException;
import org.hispindia.dhis2openimis.adapter.dhis.util.TrackedEntityQueryMaker;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.enrollment.EnrollmentBundle;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.enrollment.Enrollment;
import org.hispindia.dhis2openimis.adapter.dhis.cache.data_element.DataElementCache;

import org.hispindia.dhis2openimis.adapter.openimis.APICaller;
import org.hispindia.dhis2openimis.adapter.util.ParamsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Mithilesh Thakur
 */

@Component
//@SpringBootApplication
public class MySqlConnectionApplication {

    private static final Logger logger = LoggerFactory.getLogger(MySqlConnectionApplication.class);
    @Autowired
    APICaller apiCaller;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TrackedEntityTypeCache entityTypeCache;

    @Autowired
    private TrackedEntityAttributeCache attributeCache;

    @Autowired
    private OrganisationUnitCacheService orgUnitCache;

    @Autowired
    private ProgramCache programCache;

    @Autowired
    private ProgramStageCache programStageCache;

    @Autowired
    private DataElementCache dataElementCache;

    @Autowired
    private TrackedEntityQueryMaker queryMaker;

    @Value("${app.dhis2.api.OptionSetOptions}")
    String dhis2OptionSetURL;


    @Value("${app.dhis2.api.SyncOrganisationUnits}")
    String dhis2GetURL;

    @Value("${app.dhis2.api.TrackedEntityInstances}")
    private String teiUrl;
    
    @Value("${app.dhis2.api.Enrollments}")
    private String enrollmentsUrl;

    @Value("${app.dhis2.api.TrackedEntityInstances.Query}")
    private String teiQueryUrl;

    @Value("${app.dhis2.api.Events.Post}")
    private String eventsPostUrl;

    private HttpEntity<Void> request;
    private RestTemplate restTemplate;  
    private HttpHeaders authHeaders;

    @Autowired
    public MySqlConnectionApplication(RestTemplate restTemplate, @Qualifier("Dhis2")HttpHeaders authHeaders) {
            request = new HttpEntity<Void>(authHeaders);
            //restTemplate = new RestTemplate();
            this.restTemplate = restTemplate;
            this.authHeaders = authHeaders;
    }

    public void run(){
            
            logger.info("inside database connection: " + LocalDate.now().toString());
            //connectToDefaultMYSQLDataBase();
            String dhis2PostURL = dhis2GetURL;
            String dhis2URL = dhis2GetURL + "?" + ParamsUtil.ORG_UNITS_SYNC_PARAM + "&filter=level:eq:1";
            Map<String, String> orgUnitParentIdMap =  new HashMap<>();
            String leval2ParentId = "";
            String orgUnitAttributeId = "";
            System.out.println( "dhis2GetURL -- " + dhis2URL );
            OrganisationUnitsDhis2Bundle dhis2OrgBundle = getDhis2OrgUnitFromApi(dhis2URL);
            for(OrganisationUnitDhis2 orgUntDhis2 : dhis2OrgBundle.getOrganisationUnits() ) {

                if( orgUntDhis2.getLevel().equalsIgnoreCase("1")){
                    leval2ParentId = orgUntDhis2.getId();
                }

                for(OrganisationUnitAttributeValue orgAttrValue : orgUntDhis2.getAttributeValues() ) {
                    OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();

                    if( orgUnitAttribute.getCode().equalsIgnoreCase("amritFacilityPrimaryKey")){
                        orgUnitAttributeId = orgUnitAttribute.getId();
                    }
                }

                orgUnitParentIdMap.put(orgUntDhis2.getCode(), orgUntDhis2.getId());
                System.out.println( " dhis2 org--  " + orgUntDhis2.getDisplayName() + " -- " + orgUntDhis2.getId() + " -- " + orgUntDhis2.getLevel() + " -- " + orgUntDhis2.getCode());
            }


        String dhis2OrgUnitURLLevel4 = dhis2GetURL + "?" + ParamsUtil.ORG_UNITS_SYNC_PARAM + "&filter=level:eq:4";

        Map<String, String> level4OrgUnitIdMap =  new HashMap<>();
        System.out.println( "dhis2OrgUnitURLLevel4 -- " + dhis2OrgUnitURLLevel4 );
        OrganisationUnitsDhis2Bundle level4DHIS2OrgBundle = getDhis2OrgUnitFromApi(dhis2OrgUnitURLLevel4);
        for(OrganisationUnitDhis2 orgUntDhis2 : level4DHIS2OrgBundle.getOrganisationUnits() ) {

            for(OrganisationUnitAttributeValue orgAttrValue : orgUntDhis2.getAttributeValues() ) {
                OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();

                if( orgUntDhis2.getLevel().equalsIgnoreCase("4") && orgUnitAttribute.getCode().equalsIgnoreCase("amritFacilityPrimaryKey")){

                    level4OrgUnitIdMap.put(orgAttrValue.getValue(), orgUntDhis2.getId());
                }
            }
        }
        // optionSet option with meta atribute
        String dhis2OptionURL = dhis2OptionSetURL + "?" + ParamsUtil.OPTION_PARAM;
        System.out.println( "dhis2OptionURL -- " + dhis2OptionURL );
        Map<String, String> optionAttributeValueMap =  new HashMap<>();
        OptionDhis2Bundle optionDhis2OrgBundle = getOptionAttributeValueFromApi(dhis2OptionURL);

        for(OptionDhis2 optionDhis2 : optionDhis2OrgBundle.getOptions()) {
            for(OptionAttributeValue optionAttrValue : optionDhis2.getAttributeValues() ) {
                //OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();

                if( optionAttrValue.getAttribute().getCode().equalsIgnoreCase("PermVillageId")){

                    optionAttributeValueMap.put(optionAttrValue.getValue(), optionDhis2.getCode() );
                }
            }
        }



// orgunit import
            //level2OrgUnitAndPostToDhis2( leval2ParentId, orgUnitAttributeId, dhis2PostURL );
            //level3OrgUnitAndPostToDhis2( orgUnitAttributeId, dhis2PostURL );
            //level4OrgUnitAndPostToDhis2( orgUnitAttributeId, dhis2PostURL ); 
// end
            //getTEIAndPostToDhis2();
            getTEIAndEventAndPostToDhis2( level4OrgUnitIdMap, optionAttributeValueMap );

/*




            System.out.println(  " date - " + LocalDate.of(1990, 1, 1).toString() );
            String sql = "INSERT INTO users (fullname, email, password) VALUES (?, ?, ?)";
         
            int result = jdbcTemplate.update(sql, "Ravi Kumar", "ravi.kumar@gmail.com", "ravi2021");
         
            if (result > 0) {
                System.out.println("A new row has been inserted.");
            }
            
            //System.out.println(  " date - "  );

            //String query = "SELECT BloodGroupID,BloodGroup,BloodGroupDesc FROM m_bloodgroup; ";
            

            String query = "SELECT md.DistrictID , md.DistrictName , md.GovtDistrictID, md.GovtStateID, "
            + " ms.StateID, ms.StateName, ms.StateCode FROM m_district md "
            + " INNER JOIN m_state ms ON ms.StateID = md.StateID; ";

            System.out.println(  " query - " + query );                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer DistrictID = rs.getInt(1);
                String DistrictName = rs.getString( 2 );
                String GovtDistrictID = rs.getString( 3 );
                String GovtStateID = rs.getString( 4 );
                String StateID = rs.getString( 5 );
                String StateName = rs.getString( 6 );
                String StateCode = rs.getString( 7 );
                
                if ( DistrictID != null  )
                {
                    System.out.println( "DistrictID- " + DistrictID + " DistrictName- " + DistrictName + " GovtDistrictID- " + GovtDistrictID
                    + " GovtStateID- " + GovtStateID + " StateID- " + StateID + " StateName- " + StateName + " StateCode- " + StateCode );
                }
            }

            //apiCaller.getLegacyDdemoOpenIMISOrgAndPostToDhis2(openIMISLegacyDemoURL);
*/
	}

        /*
		SqlRowSet srs = jdbcTemplate.queryForRowSet("select * from customer");
		int rowCount = 0;
		while (srs.next()) {
			System.out.println(srs.getString("id") + " - "
					+ srs.getString("first_name") + " - "
					+ srs.getString("last_name") + " - "
					+ srs.getString("last_login"));
			rowCount++;
		}
		System.out.println("Number of records : " + rowCount);
	}
        */
  
        private void getTEIAndEventAndPostToDhis2( Map<String, String> level4OrgUnitIdMap, Map<String, String> optionAttributeValueMap ){
            try {
/*
                for(String optionMapKey : optionAttributeValueMap.keySet()) {
                    System.out.println( " option mapKey -- " + optionMapKey + " keyValue -- " + optionAttributeValueMap.get(optionMapKey));

                }
                for(String orgUnitMapKey : level4OrgUnitIdMap.keySet()) {
                    System.out.println( " orgunit mapKey -- " + orgUnitMapKey + " keyValue -- " + level4OrgUnitIdMap.get(orgUnitMapKey));

                }
*/
                String query = "SELECT i_ben_details.BeneficiaryRegID, i_ben_details.FirstName,i_ben_details.MiddleName,i_ben_details.LastName,i_ben_details.Gender, "
                               + " YEAR(i_ben_details.DOB ),i_ben_details.VanID, CAST(i_ben_details.CreatedDate AS DATE),i_ben_address.PermSubDistrictId, "
                               + " i_ben_address.PermVillageId,i_ben_address.PermVillage FROM i_beneficiarydetails i_ben_details "
                               + " INNER JOIN i_beneficiarymapping i_ben_mapping ON i_ben_mapping.BenRegId = i_ben_details.BeneficiaryRegID "
                               + " INNER JOIN i_beneficiaryaddress i_ben_address ON i_ben_address.BenAddressID = i_ben_mapping.BenAddressId "
                               + " WHERE i_ben_details.BeneficiaryRegID IS NOT NULL AND i_ben_address.PermStateId IS NOT NULL "
                               + " AND i_ben_address.PermDistrictId IS NOT NULL AND i_ben_address.PermSubDistrictId IS NOT NULL "
                               + " AND i_ben_details.BeneficiaryRegID  > 2929052 and i_ben_details.BeneficiaryRegID  <= 4696159    "
                               + " ORDER by i_ben_details.BeneficiaryRegID ";

                //System.out.println(  " query - " + query );                
                SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

                while ( rs.next() )
                {
                    String BeneficiaryRegID = rs.getString(1);
                    String FirstName = rs.getString( 2 );
                    String MiddleName = rs.getString( 3 );
                    String LastName = rs.getString( 4 );
                    String Gender = rs.getString( 5 );
                    String DOB = rs.getString( 6 );
                    String VanID = rs.getString( 7 );
                    String CreatedDate = rs.getString( 8 );
                    String PermSubDistrictId = rs.getString( 9 );
                    String PermVillageId = rs.getString( 10 );
                    String PermVillage = rs.getString( 11 );

                    if ( BeneficiaryRegID != null  )
                    {
                        String orgUnitId = level4OrgUnitIdMap.get(PermSubDistrictId);

                        TrackedEntityRequest dhis2TEIRequst = new TrackedEntityRequest();

                        dhis2TEIRequst.setOrgUnit(orgUnitId);
                        
                        List<Attribute> teiAttributes = new ArrayList<Attribute>();
                        fillTrackedEntityType(dhis2TEIRequst);
                        fillBenAttributeValue( teiAttributes, "BeneficiaryRegID", BeneficiaryRegID );
                        //fillBeneficiaryRegIDAttribute( teiAttributes, "BeneficiaryRegID", BeneficiaryRegID );
                        fillBenAttributeValue( teiAttributes, "FirstName", FirstName );
                        fillBenAttributeValue( teiAttributes, "MiddleName", MiddleName );
                        fillBenAttributeValue( teiAttributes, "LastName", LastName );
                        fillBenAttributeValue( teiAttributes, "Gender", Gender );
                        fillBenAttributeValue( teiAttributes, "DOB", DOB+"-01-01" );
                        fillBenAttributeValue( teiAttributes, "VanID", VanID );
                        fillBenAttributeValue( teiAttributes, "PermVillage", optionAttributeValueMap.get(PermVillageId) );

                        dhis2TEIRequst.setAttributes(teiAttributes);

                        TrackedEntityPostResponse teiResponse = postTrackedEntityInstance(dhis2TEIRequst);
                        //logger.info("TrackedEntityInstance posted!");

                        String orgUnit = dhis2TEIRequst.getOrgUnit();
                        String trackedEntityInstanceId = getReferenceId(teiResponse);
                        System.out.println("TEI posted trackedEntityInstanceId -- " + trackedEntityInstanceId + " BeneficiaryRegID -- " + BeneficiaryRegID);

                        //logger.debug("TrackedEntityInstance posted " + trackedEntityInstanceId );
                        EnrollmentRequestBody enrollment = buildEnrollmentBody(trackedEntityInstanceId, orgUnit, CreatedDate);
                        TrackedEntityPostResponse enrollmentResponse = postEnrollment(enrollment);
                        //logger.debug("TEI enrollment posted");

                        String enrollmentId = getReferenceId(enrollmentResponse);

                        System.out.println("TEI enrollment posted enrollmentId -- " + enrollmentId + " BeneficiaryRegID -- " + BeneficiaryRegID);
                        //CreateEventDataPojo response = new CreateEventDataPojo(trackedEntityInstanceId, orgUnit, enrollmentId);

                        // for pushing events
                         String eventQuery =  
                            " SELECT DISTINCT (db_iemr.t_bencall.BenCallID),db_iemr.t_bencall.BeneficiaryRegID," 
                            + " db_iemr.t_bencall.CallID, db_iemr.t_bencall.PhoneNo,db_iemr.t_bencall.CallTypeID,"
                            + " db_iemr.t_bencall.is1097,db_iemr.t_bencall.Category,db_iemr.t_bencall.SubCategory, "
                            + " db_iemr.t_bencall.IsOutbound,db_iemr.t_bencall.CallTime,db_iemr.t_bencall.CallEndTime, "
                            + " TIME_TO_SEC(TIMEDIFF(db_iemr.t_bencall.CallEndTime,db_iemr.t_bencall.CallTime)) AS CallDurationInSeconds,"
                            + " db_iemr.t_bencall.ReceivedRoleName,CAST(db_iemr.t_bencall.CreatedDate AS DATE),db_iemr.t_bencall.TypeOfComplaint, "
                            + " mct.CallType  FROM db_iemr.t_bencall  "
                            + " INNER JOIN db_iemr.m_calltype mct ON mct.CallTypeID = db_iemr.t_bencall.CallTypeID "
                            + " WHERE db_iemr.t_bencall.BeneficiaryRegID IS NOT NULL AND db_iemr.t_bencall.BeneficiaryRegID IN ( " + BeneficiaryRegID + " )";

                        //System.out.println(  " query - " + eventQuery );                
                        SqlRowSet eventRS = jdbcTemplate.queryForRowSet( eventQuery );

                        while ( eventRS.next() )
                        {
                            String BenCallID = eventRS.getString(1);
                            String eventBeneficiaryRegID = eventRS.getString(2);
                            String CallID = eventRS.getString(3);
                            String PhoneNo = eventRS.getString( 4 );
                            String CallTypeID = eventRS.getString( 5 );
                            String is1097 = eventRS.getString( 6 );
                            String Category = eventRS.getString( 7 );
                            String SubCategory = eventRS.getString( 8 );
                            String IsOutbound = eventRS.getString( 9 );
                            String CallTime = eventRS.getString( 10 );
                            String CallEndTime = eventRS.getString( 11 );
                            String CallDurationInSeconds = eventRS.getString( 12 );
                            String ReceivedRoleName = eventRS.getString( 13 );
                            String eventCreatedDate = eventRS.getString( 14 );
                            String TypeOfComplaint = eventRS.getString( 15 );
                            String CallType = eventRS.getString( 16 );
                            
                            if ( eventBeneficiaryRegID != null  ){
                                //System.out.println(  " BenCallID - " + BenCallID + " eventBeneficiaryRegID - " + eventBeneficiaryRegID + " CallID - " + CallID );  
                                String tempIsOutboundValue = "false";
                                if (IsOutbound.equalsIgnoreCase("0")){
                                tempIsOutboundValue = "false";
                                }
                                else{
                                tempIsOutboundValue = "true";
                                }
                                try {

                                    String programId = programCache.getIdByDisplayName("104 Health Helpline Program");
                                    String programStageId = programStageCache.getByDisplayName("Beneficiary Call Details");

                                    EventRequestBody addEventRequestBody = new EventRequestBody();
                                    addEventRequestBody.setEnrollment(enrollmentId);
                                    addEventRequestBody.setOrgUnit(orgUnitId);
                                    addEventRequestBody.setTrackedEntityInstance(trackedEntityInstanceId);
                                    addEventRequestBody.setProgram(programId);
                                    addEventRequestBody.setProgramStage(programStageId);
                                    addEventRequestBody.setStatus("ACTIVE");
                                    addEventRequestBody.setEventDate(eventCreatedDate);

                                    List<DataValue> dataValues = new ArrayList<>();
                                    fillEventDataValue(dataValues, "BenCallID", BenCallID );
                                    //fillEventDataValue(dataValues, "CallTime", CallTime );
                                    //fillEventDataValue(dataValues, "CallEndTime", CallEndTime );
                                    fillEventDataValue(dataValues, "Category", Category );
                                    fillEventDataValue(dataValues, "SubCategory", SubCategory );
                                    fillEventDataValue(dataValues, "CallType", CallType );
                                    fillEventDataValue(dataValues, "CallDurationInSeconds", CallDurationInSeconds );
                                    fillEventDataValue(dataValues, "ReceivedRoleName", ReceivedRoleName);
                                    fillEventDataValue(dataValues, "IsOutbound", tempIsOutboundValue);

                                    addEventRequestBody.setDataValues(dataValues);
                                    TrackedEntityPostResponse eventpostResponse = postNewEvent(addEventRequestBody);
                                    String eventId = getReferenceId(eventpostResponse);

                                    System.out.println( trackedEntityInstanceId + " TEI Event posted eventID -- " + eventId + " BeneficiaryRegID -- " + BeneficiaryRegID + " BenCallID -- " + BenCallID);

                                } 
                                catch(NullPointerException e) {
                                        logger.info("No new event post");
                                }
                            }
                        }

                      }
                    }

                } catch(NullPointerException e) {
                        //Do nothing as no proper mapping was found.
                }
        }

	private OrganisationUnitsDhis2Bundle getDhis2OrgUnitFromApi(String url) {

            ResponseEntity<OrganisationUnitsDhis2Bundle> responseOrgUnitDhis2 = restTemplate.exchange(url, HttpMethod.GET, request, OrganisationUnitsDhis2Bundle.class);
            return responseOrgUnitDhis2.getBody();
	}

	private OptionDhis2Bundle getOptionAttributeValueFromApi(String url) {

            ResponseEntity<OptionDhis2Bundle> responseOptionDhis2 = restTemplate.exchange(url, HttpMethod.GET, request, OptionDhis2Bundle.class);
            return responseOptionDhis2.getBody();
	}

        private void level2OrgUnitAndPostToDhis2( String leval2ParentId, String orgUnitAttributeId, String postURL ){
            try {

            String query = "SELECT StateID, StateName, StateCode, Language FROM m_state; ";
            
            System.out.println(  " query - " + query );                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer StateID = rs.getInt(1);
                String StateName = rs.getString( 2 );
                String StateCode = rs.getString( 3 );
                String Language = rs.getString( 4 );
                
                if ( StateID != null  )
                {
                    OrganisationUnitDhis2Request organisationUnitDhis2Request = new OrganisationUnitDhis2Request();
                    
                    organisationUnitDhis2Request.setName(StateName);
                    organisationUnitDhis2Request.setShortName(StateName);
                    organisationUnitDhis2Request.setCode(StateCode);
                    organisationUnitDhis2Request.setOpeningDate(LocalDate.of(1990, 1, 1).toString());
                    OrganisationUnitParentRequest orgUnitParent = new OrganisationUnitParentRequest();
                    orgUnitParent.setId(leval2ParentId);
                    organisationUnitDhis2Request.setParent( orgUnitParent );

                    List<OrganisationUnitAttributeValueRequest> attributeValues = new ArrayList<>();

                    OrganisationUnitAttributeRequest attribute = new OrganisationUnitAttributeRequest();
                    attribute.setId(orgUnitAttributeId);
                    
                    OrganisationUnitAttributeValueRequest orgAttributeValue = new OrganisationUnitAttributeValueRequest(attribute, StateID);

                    attributeValues.add(orgAttributeValue);

                    organisationUnitDhis2Request.setAttributeValues(attributeValues);

                    if( organisationUnitDhis2Request.getParent().getId() != null ){
                        logger.info(" Pushing at Level 2 to dhis2 with code - " + StateName );
                        OrganisationUnitPostResponse postOrgResponse = postToDhis2(postURL, organisationUnitDhis2Request );
                        logger.info( "post response -- " + postOrgResponse.getResponse() );
                        //System.out.println( "post response -- " + postOrgResponse.getResponse() );
                    }
                    else{
                         logger.info(" Parent not present in Amrit/Piramal at Level 2 with code - " + StateCode );
                    }
                }
            }

            } catch(NullPointerException e) {
                    //Do nothing as no proper mapping was found.
            }

        }

        private void level3OrgUnitAndPostToDhis2( String orgUnitAttributeId, String postURL ){

            String dhis2URL = dhis2GetURL + "?" + ParamsUtil.ORG_UNITS_SYNC_PARAM + "&filter=level:eq:2";
            Map<String, String> orgUnitParentIdMap =  new HashMap<>();
            
            System.out.println( "dhis2GetURL -- " + dhis2URL );
            OrganisationUnitsDhis2Bundle dhis2OrgBundle = getDhis2OrgUnitFromApi(dhis2URL);
            for(OrganisationUnitDhis2 orgUntDhis2 : dhis2OrgBundle.getOrganisationUnits() ) {

                //orgUnitParentIdMap.put(orgUntDhis2.getCode(), orgUntDhis2.getId());
                //System.out.println( " dhis2 org--  " + orgUntDhis2.getDisplayName() + " -- " + orgUntDhis2.getId() + " -- " + orgUntDhis2.getLevel() + " -- " + orgUntDhis2.getCode());
            
                for(OrganisationUnitAttributeValue orgAttrValue : orgUntDhis2.getAttributeValues() ) {
                    OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();
                    
                    if( orgUntDhis2.getLevel().equalsIgnoreCase("2") && orgUnitAttribute.getCode().equalsIgnoreCase("amritFacilityPrimaryKey")){
                        
                        orgUnitParentIdMap.put(orgAttrValue.getValue(), orgUntDhis2.getId());
                    }
            }
        }
        try {

            String query = "SELECT md.DistrictID , md.DistrictName , md.GovtDistrictID, md.GovtStateID, "
                            + " ms.StateCode, ms.LANGUAGE, md.StateID FROM m_district md "
                            + " INNER JOIN m_state ms ON ms.StateID = md.StateID; ";

            System.out.println(  " query - " + query );                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer DistrictID = rs.getInt(1);
                String DistrictName = rs.getString( 2 );
                String GovtDistrictID = rs.getString( 3 );
                String GovtStateID = rs.getString( 4 );
                String StateCode = rs.getString( 5 );
                String StateLanguage = rs.getString( 6 );
                String StateID = rs.getString( 7 );

                if ( DistrictID != null  )
                {
                    OrganisationUnitDhis2Request organisationUnitDhis2Request = new OrganisationUnitDhis2Request();

                    organisationUnitDhis2Request.setName(DistrictName);
                    organisationUnitDhis2Request.setShortName(DistrictName);
                    //organisationUnitDhis2Request.setCode(GovtDistrictID);
                    organisationUnitDhis2Request.setOpeningDate(LocalDate.of(1990, 1, 1).toString());
                    OrganisationUnitParentRequest orgUnitParent = new OrganisationUnitParentRequest();
                    orgUnitParent.setId(orgUnitParentIdMap.get(StateID));
                    organisationUnitDhis2Request.setParent( orgUnitParent );

                    List<OrganisationUnitAttributeValueRequest> attributeValues = new ArrayList<>();

                    OrganisationUnitAttributeRequest attribute = new OrganisationUnitAttributeRequest();
                    attribute.setId(orgUnitAttributeId);

                    OrganisationUnitAttributeValueRequest orgAttributeValue = new OrganisationUnitAttributeValueRequest(attribute, DistrictID );

                    attributeValues.add(orgAttributeValue);

                    organisationUnitDhis2Request.setAttributeValues(attributeValues);

                    if( organisationUnitDhis2Request.getParent().getId() != null ){
                        logger.info(" Pushing at Level 3 to dhis2 with code - " + DistrictName );
                        OrganisationUnitPostResponse postOrgResponse = postToDhis2(postURL, organisationUnitDhis2Request );
                        logger.info( "post response -- " + postOrgResponse.getResponse() );
                        //System.out.println( "post response -- " + postOrgResponse.getResponse() );
                    }
                    else{
                         logger.info(" Parent not present in Amrit/Piramal at Level 3 with code - " + StateID );
                    }
                }
            }

            } catch(NullPointerException e) {
                    //Do nothing as no proper mapping was found.
            }

        }

        private void level4OrgUnitAndPostToDhis2( String orgUnitAttributeId, String postURL ){

            String dhis2URL = dhis2GetURL + "?" + ParamsUtil.ORG_UNITS_SYNC_PARAM + "&filter=level:eq:3";;
            Map<String, String> orgUnitParentIdMap =  new HashMap<>();
            
            System.out.println( "dhis2GetURL -- " + dhis2URL );
            OrganisationUnitsDhis2Bundle dhis2OrgBundle = getDhis2OrgUnitFromApi(dhis2URL);
            for(OrganisationUnitDhis2 orgUntDhis2 : dhis2OrgBundle.getOrganisationUnits() ) {

                //orgUnitParentIdMap.put(orgUntDhis2.getCode(), orgUntDhis2.getId());
                //System.out.println( " dhis2 org--  " + orgUntDhis2.getDisplayName() + " -- " + orgUntDhis2.getId() + " -- " + orgUntDhis2.getLevel() + " -- " + orgUntDhis2.getCode());
            
                for(OrganisationUnitAttributeValue orgAttrValue : orgUntDhis2.getAttributeValues() ) {
                    OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();
                    
                    if( orgUntDhis2.getLevel().equalsIgnoreCase("3") && orgUnitAttribute.getCode().equalsIgnoreCase("amritFacilityPrimaryKey")){
                        
                        orgUnitParentIdMap.put(orgAttrValue.getValue(), orgUntDhis2.getId());
                    }
            }
        }
        try {

            String query = "SELECT mdb.BlockID, mdb.BlockName, mdb.GovSubDistrictID, md.DistrictID, "
                            + " md.GovtDistrictID FROM m_districtblock mdb "
                            + " INNER JOIN m_district md ON md.DistrictID = mdb.DistrictID; ";

            System.out.println(  " query - " + query );                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer BlockID = rs.getInt(1);
                String BlockName = rs.getString( 2 );
                String GovSubDistrictID = rs.getString( 3 );
                String DistrictID = rs.getString( 4 );
                String GovtDistrictID = rs.getString( 5 );

                if ( BlockID != null  )
                {
                    OrganisationUnitDhis2Request organisationUnitDhis2Request = new OrganisationUnitDhis2Request();

                    organisationUnitDhis2Request.setName(BlockName);
                    organisationUnitDhis2Request.setShortName(BlockName);
                    //organisationUnitDhis2Request.setCode(GovtDistrictID);
                    organisationUnitDhis2Request.setOpeningDate(LocalDate.of(1990, 1, 1).toString());
                    OrganisationUnitParentRequest orgUnitParent = new OrganisationUnitParentRequest();
                    orgUnitParent.setId(orgUnitParentIdMap.get(DistrictID));
                    organisationUnitDhis2Request.setParent( orgUnitParent );

                    List<OrganisationUnitAttributeValueRequest> attributeValues = new ArrayList<>();

                    OrganisationUnitAttributeRequest attribute = new OrganisationUnitAttributeRequest();
                    attribute.setId(orgUnitAttributeId);

                    OrganisationUnitAttributeValueRequest orgAttributeValue = new OrganisationUnitAttributeValueRequest(attribute, BlockID );

                    attributeValues.add(orgAttributeValue);

                    organisationUnitDhis2Request.setAttributeValues(attributeValues);

                    if( organisationUnitDhis2Request.getParent().getId() != null ){
                        logger.info(" Pushing at Level 4 to dhis2 with code - " + BlockName );
                        OrganisationUnitPostResponse postOrgResponse = postToDhis2(postURL, organisationUnitDhis2Request );
                        logger.info( "post response -- " + postOrgResponse.getResponse() );
                        //System.out.println( "post response -- " + postOrgResponse.getResponse() );
                    }
                    else{
                         logger.info(" Parent not present in Amrit/Piramal at Level 4 with code - " + DistrictID );
                    }
                }
            }

            } catch(NullPointerException e) {
                    //Do nothing as no proper mapping was found.
            }

        }




// -------
        private void getTEIAndPostToDhis2(){

        String dhis2OrgUnitURL = dhis2GetURL + "?" + ParamsUtil.ORG_UNITS_SYNC_PARAM + "&filter=level:eq:4";

        Map<String, String> orgUnitParentIdMap =  new HashMap<>();
        System.out.println( "dhis2GetURL -- " + dhis2OrgUnitURL );
        OrganisationUnitsDhis2Bundle dhis2OrgBundle = getDhis2OrgUnitFromApi(dhis2OrgUnitURL);
        for(OrganisationUnitDhis2 orgUntDhis2 : dhis2OrgBundle.getOrganisationUnits() ) {

            //orgUnitParentIdMap.put(orgUntDhis2.getCode(), orgUntDhis2.getId());
            //System.out.println( " dhis2 org--  " + orgUntDhis2.getDisplayName() + " -- " + orgUntDhis2.getId() + " -- " + orgUntDhis2.getLevel() + " -- " + orgUntDhis2.getCode());

            for(OrganisationUnitAttributeValue orgAttrValue : orgUntDhis2.getAttributeValues() ) {
                OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();

                if( orgUntDhis2.getLevel().equalsIgnoreCase("4") && orgUnitAttribute.getCode().equalsIgnoreCase("amritFacilityPrimaryKey")){

                    orgUnitParentIdMap.put(orgAttrValue.getValue(), orgUntDhis2.getId());
                }
            }
        }

        String dhis2OptionURL = dhis2OptionSetURL + "?" + ParamsUtil.OPTION_PARAM;
        System.out.println( "dhis2OptionURL -- " + dhis2OptionURL );
        Map<String, String> optionAttributeValueMap =  new HashMap<>();
        OptionDhis2Bundle optionDhis2OrgBundle = getOptionAttributeValueFromApi(dhis2OptionURL);

        for(OptionDhis2 optionDhis2 : optionDhis2OrgBundle.getOptions()) {
            for(OptionAttributeValue optionAttrValue : optionDhis2.getAttributeValues() ) {
                //OrganisationUnitAttribute orgUnitAttribute = orgAttrValue.getAttribute();

                if( optionAttrValue.getAttribute().getCode().equalsIgnoreCase("PermVillageId")){

                    optionAttributeValueMap.put(optionAttrValue.getValue(), optionDhis2.getCode() );
                }
            }
        }
/*
        for(String optionMapKey : optionAttributeValueMap.keySet()) {
            System.out.println( "mapKey -- " + optionMapKey + " keyValue -- " + optionAttributeValueMap.get(optionMapKey));

        }
*/
        try {

            String query = "SELECT i_ben_details.BeneficiaryRegID, i_ben_details.FirstName,i_ben_details.MiddleName,i_ben_details.LastName,i_ben_details.Gender, "
                           + " YEAR(i_ben_details.DOB ),i_ben_details.VanID, CAST(i_ben_details.CreatedDate AS DATE),i_ben_address.PermSubDistrictId, "
                           + " i_ben_address.PermVillageId,i_ben_address.PermVillage FROM i_beneficiarydetails i_ben_details "
                           + " INNER JOIN i_beneficiarymapping i_ben_mapping ON i_ben_mapping.BenRegId = i_ben_details.BeneficiaryRegID "
                           + " INNER JOIN i_beneficiaryaddress i_ben_address ON i_ben_address.BenAddressID = i_ben_mapping.BenAddressId "
                           + " WHERE i_ben_details.BeneficiaryRegID IS NOT NULL AND i_ben_address.PermStateId IS NOT NULL "
                           + " AND i_ben_address.PermDistrictId IS NOT NULL AND i_ben_address.PermSubDistrictId IS NOT NULL LIMIT 1000; ";

            System.out.println(  " query - " + query );                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

/*
String testQuery =  " SELECT db_iemr.t_bencall.* FROM db_iemr.t_bencall WHERE db_iemr.t_bencall.BeneficiaryRegID IS NOT NULL " 
+ " AND db_iemr.t_bencall.BeneficiaryRegID IN ( SELECT db_identity.i_beneficiarydetails.BeneficiaryRegID "
+ " FROM db_identity.i_beneficiarydetails )  LIMIT 1000; ";

            System.out.println(  " query - " + testQuery );                
            SqlRowSet testRS = jdbcTemplate.queryForRowSet( testQuery );
            while ( rs.next() )
            {
                String BeneficiaryRegID = rs.getString(1);
                if ( BeneficiaryRegID != null  ){
                    System.out.println(  " BeneficiaryRegID - " + BeneficiaryRegID );   
                }
            }
*/

            while ( rs.next() )
            {
                String BeneficiaryRegID = rs.getString(1);
                String FirstName = rs.getString( 2 );
                String MiddleName = rs.getString( 3 );
                String LastName = rs.getString( 4 );
                String Gender = rs.getString( 5 );
                String DOB = rs.getString( 6 );
                String VanID = rs.getString( 7 );
                String CreatedDate = rs.getString( 8 );
                String PermSubDistrictId = rs.getString( 9 );
                String PermVillageId = rs.getString( 10 );
                String PermVillage = rs.getString( 11 );


                if ( BeneficiaryRegID != null  )
                {
                    String orgUnitId = orgUnitParentIdMap.get(PermSubDistrictId);
/*
                    TrackedEntityRequest dhis2TEIRequst = new TrackedEntityRequest();
                     
                    dhis2TEIRequst.setOrgUnit(orgUnitParentIdMap.get(PermSubDistrictId));
                    //String registrationDate = insureeAdapter.getRegistrationDate(openImisPatient);
                    String orgUnitId = insureeAdapter.getOrgUnit(openImisPatient);
                    List<Attribute> teiAttributes = new ArrayList<Attribute>();
                    fillTrackedEntityType(dhis2TEIRequst);
                    fillBenAttributeValue( teiAttributes, "BeneficiaryRegID", BeneficiaryRegID );
                    //fillBeneficiaryRegIDAttribute( teiAttributes, "BeneficiaryRegID", BeneficiaryRegID );
                    fillBenAttributeValue( teiAttributes, "FirstName", FirstName );
                    fillBenAttributeValue( teiAttributes, "MiddleName", MiddleName );
                    fillBenAttributeValue( teiAttributes, "LastName", LastName );
                    fillBenAttributeValue( teiAttributes, "Gender", Gender );
                    fillBenAttributeValue( teiAttributes, "DOB", DOB+"-01-01" );
                    fillBenAttributeValue( teiAttributes, "VanID", VanID );
                    fillBenAttributeValue( teiAttributes, "PermVillage", optionAttributeValueMap.get(PermVillageId) );

                    dhis2TEIRequst.setAttributes(teiAttributes);

                    TrackedEntityPostResponse teiResponse = postTrackedEntityInstance(dhis2TEIRequst);
                    logger.debug("TrackedEntityInstance posted!");

                     String orgUnit = dhis2TEIRequst.getOrgUnit();
                     String trackedEntityInstanceId = getReferenceId(teiResponse);

                     logger.debug("TrackedEntityInstance posted " + trackedEntityInstanceId );
                     EnrollmentRequestBody enrollment = buildEnrollmentBody(trackedEntityInstanceId, orgUnit, CreatedDate);
                     TrackedEntityPostResponse enrollmentResponse = postEnrollment(enrollment);
                     logger.debug("TEI enrollment posted");

                     String enrollmentId = getReferenceId(enrollmentResponse);

                     logger.debug("TEI enrollment posted " + enrollmentId );
                     CreateEventDataPojo response = new CreateEventDataPojo(trackedEntityInstanceId, orgUnit, enrollmentId);

                    // for pushing events
                     String eventQuery =  " SELECT db_iemr.t_bencall.BenCallID, db_iemr.t_bencall.BeneficiaryRegID, db_iemr.t_bencall.CallID FROM db_iemr.t_bencall WHERE db_iemr.t_bencall.BeneficiaryRegID IS NOT NULL " 
                                        + " AND db_iemr.t_bencall.BeneficiaryRegID IN ( " + BeneficiaryRegID + " )";


                    //System.out.println(  " query - " + eventQuery );                
                    SqlRowSet eventRS = jdbcTemplate.queryForRowSet( eventQuery );

                    while ( eventRS.next() )
                    {
                        String BenCallID = eventRS.getString(1);
                        String eventBeneficiaryRegID = eventRS.getString(2);
                        String CallID = eventRS.getString(3);
                        if ( eventBeneficiaryRegID != null  ){
                            System.out.println(  " BenCallID - " + BenCallID + " eventBeneficiaryRegID - " + eventBeneficiaryRegID + " CallID - " + CallID );  

                        }
                    }
        */
                    try {
                        String trackedEntityInstanceId = getTrackedEntityInstanceId(orgUnitId, BeneficiaryRegID);
                        System.out.println(  " trackedEntityInstanceId - " + trackedEntityInstanceId  );
                        String programId = programCache.getIdByDisplayName("104 Health Helpline Program");
                        String urlParam = "?program=" + programId + "&ou=" + orgUnitId + "&trackedEntityInstance=" + trackedEntityInstanceId +"&paging=false&";
                        String getEnrollmentUrl = enrollmentsUrl + urlParam + ParamsUtil.ENROLLMENT_PARAM;
                        EnrollmentBundle enrollmentOrgBundle = getTEIEnrollmentValueFromApi(getEnrollmentUrl);
                        String enrollment = enrollmentOrgBundle.getEnrollments().get(0).getEnrollment();
                        System.out.println(  " enrollment - " + enrollment  );
                        String programStageId = programStageCache.getByDisplayName("Beneficiary Call Details");

                        EventRequestBody addEventRequestBody = new EventRequestBody();
                        addEventRequestBody.setEnrollment(enrollment);
                        addEventRequestBody.setOrgUnit(orgUnitId);
                        addEventRequestBody.setTrackedEntityInstance(trackedEntityInstanceId);
                        addEventRequestBody.setProgram(programId);
                        addEventRequestBody.setProgramStage(programStageId);
                        addEventRequestBody.setStatus("ACTIVE");
                        addEventRequestBody.setEventDate(LocalDate.now().toString());

                        List<DataValue> dataValues = new ArrayList<>();
                        fillEventDataValue(dataValues, "ReceivedRoleName", "HYBRID HAO");
                        fillEventDataValue(dataValues, "IsOutbound", "true");
                        addEventRequestBody.setDataValues(dataValues);
                        TrackedEntityPostResponse eventpostResponse = postNewEvent(addEventRequestBody);
                        String eventId = getReferenceId(eventpostResponse);

                        logger.debug("TEI New Event posted " + eventId );
                                             
                    } 
                    catch(ObjectNotFoundException e) {
                            logger.info("No active insuree found! Posting a new insuree");
                    }
                    catch(InternalException e) {
                            logger.info("No active insuree found! Posting a new insuree");
                    }
                    
                  }
                }

            } catch(NullPointerException e) {
                    //Do nothing as no proper mapping was found.
            }

        }

        private void fillTrackedEntityType(TrackedEntityRequest dhisInsuree) {
            String trackedEntityType = entityTypeCache.getByDisplayName("Person");
            dhisInsuree.setTrackedEntityType(trackedEntityType);
        }
        private void fillBenAttributeValue(List<Attribute> teiAttributes, String attributeCode, String attributeValue ) {
            String attribute = attributeCache.get(attributeCode);
            String value;
            try {
                    value = attributeValue;
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            teiAttributes.add(new Attribute<String>(attribute, value));
        }

        private void fillBenFirstNameAttribute(List<Attribute> teiAttributes, String FirstName, String attributeValue ) {
            String attribute = attributeCache.get(FirstName);
            String value;
            try {
                    value = attributeValue;
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            teiAttributes.add(new Attribute<String>(attribute, value));
        }

        private void fillBeneficiaryRegIDAttribute(List<Attribute> teiAttributes, String BeneficiaryRegID, String attributeValue ) {
            String attribute = attributeCache.get(BeneficiaryRegID);
            String value;
            try {
                    value = attributeValue;
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            teiAttributes.add(new Attribute<String>(attribute, value));
        }
        private void fillBeneficiaryIdAttribute(List<Attribute> teiAttributes, String beneficiary_id, String attributeValue ) {
            String attribute = attributeCache.get("beneficiary_id");
            String value;
            try {
                    value = attributeValue;
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            teiAttributes.add(new Attribute<String>(attribute, value));
        }
        private void fillBenDobAttribute(List<Attribute> teiAttributes, String ben_dob, String attributeValue ) {
            String attribute = attributeCache.get("ben_dob");
            String value;
            try {
                    value = attributeValue;
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            teiAttributes.add(new Attribute<String>(attribute, value));
        }

        private void fillEventDataValue(List<DataValue> dataValues, String deCode, String eventDataValue ) {
            //String attribute = attributeCache.get(attributeCode);
            String dataElementId = dataElementCache.get(deCode);
            String value;
            try {
                    value = eventDataValue;
                    
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            DataValue<String> dataValue = new DataValue<>(dataElementId, value);
            dataValues.add(dataValue);
            
        }

        private void fillBenGenderAttribute(List<Attribute> teiAttributes, String ben_gender, String attributeValue ) {
            String attribute = attributeCache.get("ben_gender");
            String value;
            try {
                    value = attributeValue;
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                    value = "";
            }
            teiAttributes.add(new Attribute<String>(attribute, value));
        }

        private OrganisationUnitPostResponse postToDhis2(String dhis2PostURL, OrganisationUnitDhis2Request organisationUnitDhis2Request) {
            
            restTemplate = new RestTemplate();
            HttpEntity<OrganisationUnitDhis2Request> postRrequest = new HttpEntity<>(organisationUnitDhis2Request, authHeaders);
            
            logger.debug("Posting orgUnit: " + organisationUnitDhis2Request.toString() + " to url: " + dhis2PostURL );
            ResponseEntity<OrganisationUnitPostResponse> response = restTemplate.exchange(dhis2PostURL, HttpMethod.POST, postRrequest, OrganisationUnitPostResponse.class);

            return response.getBody();
        }

	private TrackedEntityPostResponse postTrackedEntityInstance(TrackedEntityRequest dhis2TEIRequst) {
		HttpEntity<TrackedEntityRequest> request = new HttpEntity<TrackedEntityRequest>(dhis2TEIRequst, authHeaders);
		//logger.debug("Posting TEI: " + dhis2TEIRequst.toString() + " to url: " + teiUrl);
		ResponseEntity<TrackedEntityPostResponse> response = 
				restTemplate.exchange(teiUrl, HttpMethod.POST, request, TrackedEntityPostResponse.class);
		
		return response.getBody();
	}

        private String getReferenceId(TrackedEntityPostResponse response) {
            return response.getResponse().getImportSummaries().get(0).getReference();
        }


        private EnrollmentRequestBody buildEnrollmentBody(String referenceId, String orgUnit, String registrationDate) {
            EnrollmentRequestBody enrollment = new EnrollmentRequestBody();
            enrollment.setTrackedEntityInstance(referenceId);
            enrollment.setOrgUnit(orgUnit);
            enrollment.setStatus("ACTIVE");
            enrollment.setEnrollmentDate(registrationDate);
            enrollment.setIncidentDate(registrationDate);
            String programId = programCache.getIdByDisplayName("104 Health Helpline Program");
            //programStageCache.getByDisplayName(programId)
            enrollment.setProgram(programId);

            return enrollment;
        }

        private TrackedEntityPostResponse postEnrollment(EnrollmentRequestBody enrollment) {
            HttpEntity<EnrollmentRequestBody> request = new HttpEntity<>(enrollment, authHeaders);

            //logger.debug("Posting Enrollment: " + enrollment.toString() + " to url: " + enrollmentsUrl);
            ResponseEntity<TrackedEntityPostResponse> response = 
                            restTemplate.exchange(enrollmentsUrl, HttpMethod.POST, request, TrackedEntityPostResponse.class);

            return response.getBody();
        }


	public String getTrackedEntityInstanceId(String orgUnitId, String attributeValue) throws ObjectNotFoundException, InternalException {
		String url = constructUrl(orgUnitId, attributeValue);
                //System.out.println(  " TEI URL - " + url  );         
		return queryMaker.queryAndGetTrackedEntityInstanceId(url);
	}
	private String constructUrl(String orgUnitId, String attributeValue) {
		StringBuilder url = new StringBuilder(teiQueryUrl);
		url.append("ou=" + orgUnitId);
		//url.append("&ouMode=ACCESSIBLE");
                url.append("&ouMode=DESCENDANTS");

		
		String programId = programCache.getIdByDisplayName("104 Health Helpline Program");
		url.append("&program=" + programId);
				
		String insureeIdAttributeId = attributeCache.get("BeneficiaryRegID");
		url.append("&attribute=" + insureeIdAttributeId + ":EQ:" + attributeValue);
		
		url.append("&paging=false");
                //System.out.println(  " TEI URL - " + url.toString()  );
		return url.toString();
	}


	private EnrollmentBundle getTEIEnrollmentValueFromApi(String url) {

            ResponseEntity<EnrollmentBundle> responseEnrollmentBundle = restTemplate.exchange(url, HttpMethod.GET, request, EnrollmentBundle.class);
            return responseEnrollmentBundle.getBody();
	}

	private TrackedEntityPostResponse postNewEvent(EventRequestBody addEventRequestBody) {
		HttpEntity<EventRequestBody> request = new HttpEntity<>(addEventRequestBody, authHeaders);
		
		//logger.debug("Creating an event with request body: " + addEventRequestBody.toString());
		ResponseEntity<TrackedEntityPostResponse> response = restTemplate.exchange( eventsPostUrl, HttpMethod.POST, request, TrackedEntityPostResponse.class);
		return response.getBody();
	}

	


        private void connectToDefaultMYSQLDataBase(){
            try {

            String query = "SELECT StateID, StateName, StateCode, Language FROM m_state; ";

            System.out.println(  " query - " + query );                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
           
            while ( rs.next() )
            {
                Integer StateID = rs.getInt(1);
                String StateName = rs.getString( 2 );
                String StateCode = rs.getString( 3 );
                String Language = rs.getString( 4 );
                //String Population = rs.getString( 5 );
               
             
                if ( StateID != null  )
                {
                    System.out.println( "StateID- " + StateID + " StateName - " + StateName + " StateCode - " + StateCode
                    + " Language - " + Language );
                }
            }

            } catch(NullPointerException e) {
                    //Do nothing as no proper mapping was found.
            }
        }         
}
