package pt.iul.ista.ads.models;

import java.util.List;

public class RelationshipsResponseModel extends GetOperationBaseResponseModel<List<RelationshipsResponseModel.RelationshipModel>> {

	public static class RelationshipModel {
		private String name;
		private String className1;
		private String className2;
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
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
}
