package pt.iul.ista.ads.models;

import java.util.List;

public class ClassesModel {

	private String name;
	
	private List<ClassesModel> subclasses;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ClassesModel> getSubclasses() {
		return subclasses;
	}

	public void setSubclasses(List<ClassesModel> subclasses) {
		this.subclasses = subclasses;
	}
	
}
