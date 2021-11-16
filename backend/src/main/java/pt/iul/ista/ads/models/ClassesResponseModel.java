package pt.iul.ista.ads.models;

import java.util.List;

public class ClassesResponseModel {

	private String name;
	
	private List<ClassesResponseModel> subclasses;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ClassesResponseModel> getSubclasses() {
		return subclasses;
	}

	public void setSubclasses(List<ClassesResponseModel> subclasses) {
		this.subclasses = subclasses;
	}
	
}
