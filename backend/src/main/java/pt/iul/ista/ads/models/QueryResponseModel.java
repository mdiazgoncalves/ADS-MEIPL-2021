package pt.iul.ista.ads.models;

import java.util.List;

public class QueryResponseModel extends GetOperationBaseResponseModel<List<QueryResponseModel.QueryResponse>> {

	public static class QueryResponse {
		private String variableName;
		private List<String> results;
		public String getVariableName() {
			return variableName;
		}
		public void setVariableName(String variableName) {
			this.variableName = variableName;
		}
		public List<String> getResults() {
			return results;
		}
		public void setResults(List<String> results) {
			this.results = results;
		}
	}
}
