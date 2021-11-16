package pt.iul.ista.ads.models;

public class RelationshipAlterRequestModel {

	private String newRelationshipName;
	private String className1;
	private String className2;
	public String getNewRelationshipName() {
		return newRelationshipName;
	}
	public void setNewRelationshipName(String newRelationshipName) {
		this.newRelationshipName = newRelationshipName;
	}
	public String getClassName1() {
		return className1;
	}
	public void setClassName1(String className1) {
		this.className1 = className1;
	}
	public String getClassName2() {
		return className2;
	}
	public void setClassName2(String className2) {
		this.className2 = className2;
	}
}
