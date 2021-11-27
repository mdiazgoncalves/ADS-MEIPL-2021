package pt.iul.ista.ads.models;

public class RelationshipDetailResponseModel extends GetOperationBaseResponseModel<RelationshipDetailResponseModel.RelationshipModel>{

	public static class RelationshipModel {
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
