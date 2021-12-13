package pt.iul.ista.ads.models;

import java.util.List;

public class IndividualModel {
	private String individualName;
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

	public String getIndividualName() {
		return individualName;
	}

	public void setIndividualName(String individualName) {
		this.individualName = individualName;
	}
}
