package pt.iul.ista.ads.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import pt.iul.ista.ads.github.GithubAccessToken;
import pt.iul.ista.ads.github.GithubOperations;
import pt.iul.ista.ads.github.InvalidBranchException;
import pt.iul.ista.ads.github.OldCommitException;
import pt.iul.ista.ads.owl.OntologyException;

public class Tests {

	@Test
	public void testGithubAccess() throws IOException {
		String token = GithubAccessToken.getGithubAccessToken();
		GitHub github = new GitHubBuilder().withAppInstallationToken(token).build();
		GHRepository repository = github.getRepository("ads-meipl/knowledge-base");
	}

	@Test
	public void testEditOntology() throws OldCommitException, IOException, OntologyException, InvalidBranchException {
		String testBranch = "test@example.com";
		String commit = GithubOperations.getLatestCommit(testBranch);
		GithubOperations.editOntology(testBranch, commit, (ontology) -> {
			int randomNum = ThreadLocalRandom.current().nextInt(0, 1000000);
			ontology.addClass("TestClass" + randomNum, null);
		});
		// TODO verificar se a classe de facto é criada
	}
}
