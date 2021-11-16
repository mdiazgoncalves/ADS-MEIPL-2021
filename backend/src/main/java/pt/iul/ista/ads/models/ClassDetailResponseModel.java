package pt.iul.ista.ads.models;

import java.util.List;

public class ClassDetailResponseModel {

	private String className;
	private String superClass;
	private List<String> individuals;
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public List<String> getIndividuals() {
		return individuals;
	}
	public void setIndividuals(List<String> individuals) {
		this.individuals = individuals;
	}
	public String getSuperClass() {
		return superClass;
	}
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	
	
}
