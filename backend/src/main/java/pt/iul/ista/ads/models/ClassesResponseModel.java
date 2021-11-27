package pt.iul.ista.ads.models;

import java.util.List;

public class ClassesResponseModel extends GetOperationBaseResponseModel<List<ClassesResponseModel.ClassModel>> {

	public static class ClassModel {
		private String className;
		private List<ClassModel> subclasses;
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public List<ClassModel> getSubclasses() {
			return subclasses;
		}
		public void setSubclasses(List<ClassModel> subclasses) {
			this.subclasses = subclasses;
		}
	}
}
