package org.hispindia.dhis2openimis.adapter.dhis.cache.attribute_options;

import java.util.List;

import org.hispindia.dhis2openimis.adapter.dhis.cache.data_element.DataElementCache;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.data_element.DataElement;
import org.hispindia.dhis2openimis.adapter.dhis.pojo.tracked_entity.TrackedEntityAttribute;
import org.hispindia.dhis2openimis.adapter.util.exception.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingService {

	@Autowired
	private BeneficiaryGenderCacheService beneficiaryGenderCache;
	
	@Autowired
	private BeneficiaryVillageNameCacheService beneficiaryVillageNameCache;

	@Autowired
	private DiagnosisCacheService diagnosisCache;
	
	@Autowired
	private EducationCacheService educationCache;
	
	@Autowired
	private GenderCacheService genderCache;
	
	@Autowired
	private PovertyStatusCache povertyStatusCache;
	
	@Autowired
	private FamilyGroupTypeCacheService familyGroupCache;
	
	@Autowired
	private HouseholdHeadCacheService householdHeadCache;
	
	@Autowired
	private IdentificationTypeCacheService idTypeCache;
	
	@Autowired
	private MaritalStatusCacheService maritalStatusCache;
	
	@Autowired
	private ProfessionCacheService professionCache;
	
	@Autowired
	private VisitTypeCacheService visitTypeCache;

	@Autowired
	private PolicyProductCache policyProductCache;

	@Autowired
	private PolicyStageCache policyStageCache;

	@Autowired
	private PolicyStatusCache policyStatusCache;

	@Autowired
        private ReceivedRoleNameCache receivedRoleNameCache;

	@Autowired
	private ClaimStatusCache claimStatusCache;
	
	
	private TrackedEntityAttributeOptionsCache getCacheService(String attributeType) throws InternalException {
		TrackedEntityAttributeOptionsCache cache;
		
		switch(attributeType) {
			case "Beneficiary Gender":
				cache = beneficiaryGenderCache;
				break;
			case "Beneficiary Village Name":
				cache = beneficiaryVillageNameCache;
				break;
			case "Diagnosis":
				cache = diagnosisCache;
				break;
			case "Education":
				cache = educationCache;
				break;
			case "Family/Group type":
				cache = familyGroupCache;
				break;
			case "Gender":
				cache = genderCache;
				break;
			case "Household head":
				cache = householdHeadCache;
				break;
			case "Identification type":
				cache = idTypeCache;
				break;
			case "Marital status":
				cache = maritalStatusCache;
				break;
			case "Poverty status":
				cache = povertyStatusCache;
				break;
			case "Profession":
				cache = professionCache;
				break;
			case "Visit type":
				cache = visitTypeCache;
				break;
			case "Secondary diagnosis 1":
				throw new InternalException("It will be same as for case 'Diagnosis' so no need to enter");
			case "Secondary diagnosis 2":
				throw new InternalException("It will be same as for case 'Diagnosis' so no need to enter");
			case "Secondary diagnosis 3":
				throw new InternalException("It will be same as for case 'Diagnosis' so no need to enter");
			case "Secondary diagnosis 4":
				throw new InternalException("It will be same as for case 'Diagnosis' so no need to enter");
			default:
				throw new IllegalArgumentException("Received an unmapped TrackedEntityAttribute: " + attributeType);
		}
		
		return cache;
	}

	public DataElementOptionsCache getDataElementsCache(String optionType) {
		DataElementOptionsCache cache;

		switch (optionType) {
			case "Received Role Name_104":
				cache = receivedRoleNameCache;
				break;
			case "Call Category_104":
				cache = policyStatusCache;
				break;
			case "Call Sub Category_104":
				cache = policyStageCache;
				break;
			case "Call Type_104":
				cache = claimStatusCache;
				break;
			case "Disease Type_104":
				cache = policyProductCache;
				break;
			default:
				throw new IllegalArgumentException("Received an unmapped TrackedEntityAttribute: " + optionType);
		}
		return cache;
	}
	
	
	public void saveInCache(String attributeType, List<TrackedEntityAttribute> attributes) {
		try {
			TrackedEntityAttributeOptionsCache cache = getCacheService(attributeType);
			cache.save(attributes);
		} catch(InternalException e) {
			//Do nothing. This is to handle Secondary Diagnosis.
			//It will be same as for case 'Diagnosis' so no need to enter
		}
	}

	public void saveDataElementsInCache(String attributeType, List<DataElement> attributes) {
		DataElementOptionsCache cache = getDataElementsCache(attributeType);
		cache.save(attributes);
	}
}
