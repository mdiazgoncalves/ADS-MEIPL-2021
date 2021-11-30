package pt.iul.ista.ads.models;

public class ErrorResponseModel {
	private String errorMessage;
	
	public ErrorResponseModel() {}
	
	public ErrorResponseModel(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
