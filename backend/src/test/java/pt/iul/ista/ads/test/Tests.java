package pt.iul.ista.ads.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import pt.iul.ista.ads.github.GithubAccessToken;

public class Tests {

	@Test
	public void testGithubAccess() {
		try {
			String token = GithubAccessToken.getGithubAccessToken();
			GitHub github = new GitHubBuilder().withAppInstallationToken(token).build();
			GHRepository repository = github.getRepository("ads-meipl/knowledge-base");
		} catch(IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
	}

}
