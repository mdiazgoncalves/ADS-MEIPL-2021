package pt.iul.ista.ads.models;

import java.util.List;

public class ClassesResponseModel extends GetOperationBaseResponseModel<List<ClassesResponseModel.ClassTreeModel>> {

	public static class ClassTreeModel {
		private String className;
		private List<ClassTreeModel> subclasses;
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public List<ClassTreeModel> getSubclasses() {
			return subclasses;
		}
		public void setSubclasses(List<ClassTreeModel> subclasses) {
			this.subclasses = subclasses;
		}
	}
}
