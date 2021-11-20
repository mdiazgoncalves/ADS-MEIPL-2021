package pt.iul.ista.ads.github;

public class OldCommitException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String latestCommit;
	
	public OldCommitException(String latestCommit) {
		this.latestCommit = latestCommit;
	}

	public String getLatestCommit() {
		return latestCommit;
	}

	public void setLatestCommit(String latestCommit) {
		this.latestCommit = latestCommit;
	}

}
