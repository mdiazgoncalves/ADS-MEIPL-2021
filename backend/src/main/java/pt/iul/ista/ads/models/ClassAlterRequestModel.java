package pt.iul.ista.ads.models;

public class ClassAlterRequestModel {
	private String newSuperClass;
	private String newClassName;
	public String getNewSuperClass() {
		return newSuperClass;
	}
	public void setNewSuperClass(String newSuperClass) {
		this.newSuperClass = newSuperClass;
	}
	public String getNewClassName() {
		return newClassName;
	}
	public void setNewClassName(String newClassName) {
		this.newClassName = newClassName;
	}
}
