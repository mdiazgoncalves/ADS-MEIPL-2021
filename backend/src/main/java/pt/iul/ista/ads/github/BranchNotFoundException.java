package pt.iul.ista.ads.github;

public class BranchNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public BranchNotFoundException(String branch) {
		super("Branch '" + branch + "' does not exist");
	}
}
