package pt.iul.ista.ads.models;

import java.util.List;

public class IndividualAlterRequestModel {

	private String newIndividualName;
	private String className;
	private List<IndividualRelationships> relationships;
	
	static class IndividualRelationships {
		private String relationshipName;
		private String individual2;
		public String getRelationshipName() {
			return relationshipName;
		}
		public void setRelationshipName(String relationshipName) {
			this.relationshipName = relationshipName;
		}
		public String getIndividual2() {
			return individual2;
		}
		public void setIndividual2(String individual2) {
			this.individual2 = individual2;
		}
	}

	public String getNewIndividualName() {
		return newIndividualName;
	}

	public void setNewIndividualName(String newIndividualName) {
		this.newIndividualName = newIndividualName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<IndividualRelationships> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<IndividualRelationships> relationships) {
		this.relationships = relationships;
	}
}
