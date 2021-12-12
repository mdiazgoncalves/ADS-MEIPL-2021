package pt.iul.ista.ads.models;

public class BranchResponseModel {

	private String branchName;
	private String lastCommitDate;
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getLastCommitDate() {
		return lastCommitDate;
	}
	public void setLastCommitDate(String lastCommitDate) {
		this.lastCommitDate = lastCommitDate;
	}
	
}
