package pt.iul.ista.ads.models;

import java.util.List;

public class IndividualCreateRequestModel {
	
	private String className;
	private List<IndividualRelationshipModel> relationships;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<IndividualRelationshipModel> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<IndividualRelationshipModel> relationships) {
		this.relationships = relationships;
	}

}
