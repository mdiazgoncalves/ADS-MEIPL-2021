package pt.iul.ista.ads.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.kohsuke.github.GHBlob;
import org.kohsuke.github.GHContentUpdateResponse;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.iul.ista.ads.owl.Ontology;
import pt.iul.ista.ads.owl.OntologyException;

public class GithubOperations {

	private static ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();
	
	private static GHRepository repository;
	
	private static final String owlPath = "knowledge-base.owl";
	
	private static Logger logger = LoggerFactory.getLogger(GithubOperations.class);
	
	private static String token;	// TODO remover isto, a cache do access token deve ser responsabilidade de GithubAccessToken
	
	static {
		try {
			token = GithubAccessToken.getGithubAccessToken();
			System.out.println("Github token:");
			System.out.println(token);
//			token = "ghs_PhUY5JueK2maAdgocndGEZG1i3oKCG0odPlW";
			// TODO será necessário voltar a fazer este procedimento quando o access token expirar
			GitHub github = new GitHubBuilder().withAppInstallationToken(token).build();
			repository = github.getRepository("ads-meipl/knowledge-base");
		} catch(IOException e) {
			e.printStackTrace();
			logger.error("Erro na inicialização de GithubOperations");
		}
	}
	
	// retorna hash do commit efetuado
	public static String editOntology(String branch, String commit, OntologyEditorCallback callback) throws OldCommitException, IOException, OntologyException, InvalidBranchException {
		// check if branch is valid
		if(!isValidBranch(branch))
			throw new InvalidBranchException();
		
		lockBranch(branch);
		try {
			// check if commit is the latest commit
			String latestCommit = getLatestCommit(branch);
			if(!commit.equals(latestCommit))
				throw new OldCommitException(latestCommit, branch);
			
			// get ontology from repo
			GHTree tree = repository.getTree(branch);
			GHBlob owlBlob = null;
			for(GHTreeEntry entry : tree.getTree())
				if(entry.getPath().equals(owlPath))
					owlBlob = entry.asBlob();
			String origOwl = new String(Base64.getMimeDecoder().decode(owlBlob.getContent()));
			Ontology ontology = new Ontology(origOwl);
			
			// invoke callback to modify ontology
			callback.execute(ontology);
			
			// push new ontology to repo
			String newOwl = ontology.toString();
			GHContentUpdateResponse res = repository.createContent()
				.branch(branch)
				.path(owlPath)
				.content(newOwl)
				.message("User update through the application")
				.sha(owlBlob.getSha())
				.commit();
			return res.getCommit().getSHA1();
		} finally {
			unlockBranch(branch);
		}
	}
	
	private static void lockBranch(String branch) {
		locks.putIfAbsent(branch, new ReentrantLock());
		locks.get(branch).lock();
	}
	
	private static void unlockBranch(String branch) {
		locks.get(branch).unlock();
	}
	
	public static String getLatestCommit(String branch) throws IOException {
		return repository.getCommit(branch).getSHA1();
	}
	
	public static boolean isValidBranch(String branch) {
		return !branch.equals(repository.getDefaultBranch());
	}

	public static void createBranch(String branch) throws InvalidBranchException, IOException, BranchAlreadyExistsException {
		if(!isValidBranch(branch))
			throw new InvalidBranchException();

		lockBranch(branch);
		try {			
			String latestCommitInMain = getLatestCommit(repository.getDefaultBranch());

			// não encontrei forma de criar branch com a biblioteca java
			// portanto vamos usar aqui a API REST diretamente
			
			String authorizationHeader = "Bearer " + token;
			
			HttpClient client = HttpClient.newBuilder()
					.followRedirects(Redirect.NORMAL)
					.version(Version.HTTP_1_1)
					.build();
			
			JsonObject bodyObject = new JsonObject();
			bodyObject.addProperty("ref", "refs/heads/" + branch);
			bodyObject.addProperty("sha", latestCommitInMain);
			String body = bodyObject.toString();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI("https://api.github.com/repos/ads-meipl/knowledge-base/git/refs"))
					.setHeader("Authorization", authorizationHeader)
					.POST(BodyPublishers.ofString(body))
					.build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			
			if(response.statusCode() == 422) {
				JsonObject responseObject = JsonParser.parseString(response.body()).getAsJsonObject();
				String message = responseObject.get("message").getAsString();
				if(message.contains("is not a valid ref name")) {
					throw new InvalidBranchException();
				} else if(message.contains("Reference already exists")) {
					throw new BranchAlreadyExistsException();
				} else {
					throw new RuntimeException(message);
				}
			}
		} catch(URISyntaxException | InterruptedException e) {
				throw new RuntimeException(e);
		} finally {
			unlockBranch(branch);
		}
		
	}
}
