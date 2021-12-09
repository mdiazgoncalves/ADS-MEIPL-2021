package pt.iul.ista.ads.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import pt.iul.ista.ads.github.BranchNotFoundException;
import pt.iul.ista.ads.github.GithubOperations;
import pt.iul.ista.ads.github.InvalidBranchException;
import pt.iul.ista.ads.github.OldCommitException;
import pt.iul.ista.ads.owl.OntologyException;

public class Tests {

	// O access token agora é gerido por uma classe não pública
	// Por isso não podemos fazer este teste aqui.
//	@Test
//	public void testGithubAccess() throws IOException {
//		String token = GithubOperationsBase.getGithubAccessToken();
//		GitHub github = new GitHubBuilder().withAppInstallationToken(token).build();
//		GHRepository repository = github.getRepository("ads-meipl/knowledge-base");
//	}

	@Test
	public void testEditOntology() throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String testBranch = "test@example.com";
		String commit = GithubOperations.getLatestCommit(testBranch);
		GithubOperations.editOntology(testBranch, commit, (ontology) -> {
			int randomNum = ThreadLocalRandom.current().nextInt(0, 1000000);
			ontology.addClass("TestClass" + randomNum, null);
		});
		// TODO verificar se a classe de facto é criada
	}
}
