package pt.iul.ista.ads.models;

public class LatestCommitResponseModel {
	
	private String branch;
	private String latestCommit;
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getLatestCommit() {
		return latestCommit;
	}
	public void setLatestCommit(String latestCommit) {
		this.latestCommit = latestCommit;
	}

}
