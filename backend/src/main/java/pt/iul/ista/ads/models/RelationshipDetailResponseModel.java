package pt.iul.ista.ads.models;

import java.util.List;

public class RelationshipDetailResponseModel extends GetOperationBaseResponseModel<List<RelationshipDetailResponseModel.RelationshipInstanceModel>> {

	public static class RelationshipInstanceModel {
		private String individual1;
		private String individual2;
		public String getIndividual1() {
			return individual1;
		}
		public void setIndividual1(String individual1) {
			this.individual1 = individual1;
		}
		public String getIndividual2() {
			return individual2;
		}
		public void setIndividual2(String individual2) {
			this.individual2 = individual2;
		}
	}
	
}
