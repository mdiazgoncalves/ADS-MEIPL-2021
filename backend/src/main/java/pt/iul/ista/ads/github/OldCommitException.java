package pt.iul.ista.ads.github;

public class OldCommitException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String latestCommit;
	private String branch;
	
	public OldCommitException(String latestCommit, String branch) {
		this.latestCommit = latestCommit;
		this.branch = branch;
	}

	public String getLatestCommit() {
		return latestCommit;
	}

	public void setLatestCommit(String latestCommit) {
		this.latestCommit = latestCommit;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

}
