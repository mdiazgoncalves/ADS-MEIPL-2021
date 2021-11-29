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
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.iul.ista.ads.owl.Ontology;
import pt.iul.ista.ads.owl.OntologyException;

public class GithubOperations extends GithubOperationsBase {

	private static ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();
	
	private static final String owlPath = "knowledge-base.owl";
	
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

			GetOWLResponse getOWLResponse = getOWL(branch);
			Ontology ontology = new Ontology(getOWLResponse.owl);
			
			// invoke callback to modify ontology
			callback.execute(ontology);
			
			// push new ontology to repo
			String newOwl = ontology.toString();
			GHContentUpdateResponse res = getGHRepository().createContent()
				.branch(branch)
				.path(owlPath)
				.content(newOwl)
				.message("User update through the application")
				.sha(getOWLResponse.sha)
				.commit();
			return res.getCommit().getSHA1();
		} finally {
			unlockBranch(branch);
		}
	}
	
	public static ReadOntologyResponse readOntology(String branch) throws IOException, OntologyException {
		lockBranch(branch);
		try {
			String latestCommit = getLatestCommit(branch);
			GetOWLResponse getOWLResponse = getOWL(branch);
			Ontology ontology = new Ontology(getOWLResponse.owl);
			ReadOntologyResponse res = new ReadOntologyResponse();
			res.setLatestCommit(latestCommit);
			res.setOntology(ontology);
			return res;
		} finally {
			unlockBranch(branch);
		}
	}
	
	public static class ReadOntologyResponse {
		private Ontology ontology;
		private String latestCommit;
		public Ontology getOntology() {
			return ontology;
		}
		public void setOntology(Ontology ontology) {
			this.ontology = ontology;
		}
		public String getLatestCommit() {
			return latestCommit;
		}
		public void setLatestCommit(String latesetCommit) {
			this.latestCommit = latesetCommit;
		}
	}
	
	private static GetOWLResponse getOWL(String branch) throws IOException {		
		// get ontology from repo
		GHTree tree = getGHRepository().getTree(branch);
		GHBlob owlBlob = null;
		for(GHTreeEntry entry : tree.getTree())
			if(entry.getPath().equals(owlPath))
				owlBlob = entry.asBlob();
		String owl = new String(Base64.getMimeDecoder().decode(owlBlob.getContent()));

		GetOWLResponse res = new GetOWLResponse();
		res.sha = owlBlob.getSha();
		res.owl = owl;
		return res;
	}
	
	private static class GetOWLResponse {
		public String sha;
		public String owl;
	}
	
	private static void lockBranch(String branch) {
		locks.putIfAbsent(branch, new ReentrantLock());
		locks.get(branch).lock();
	}
	
	private static void unlockBranch(String branch) {
		locks.get(branch).unlock();
	}
	
	public static String getLatestCommit(String branch) throws IOException {
		// versão original deste método, usando a biblioteca java:
		//return repository.getCommit(branch).getSHA1();
		// por vezes não funciona como desejado, por isso vamos usar a api REST diretamente
		try {
			String authorizationHeader = "Bearer " + getGithubAccessToken();
			
			HttpClient client = HttpClient.newBuilder()
					.followRedirects(Redirect.NORMAL)
					.version(Version.HTTP_1_1)
					.build();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI("https://api.github.com/repos/ads-meipl/knowledge-base/commits/" + branch))
					.setHeader("Authorization", authorizationHeader)
					.build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			JsonObject responseObject = JsonParser.parseString(response.body()).getAsJsonObject();
			return responseObject.get("sha").getAsString();
		} catch(URISyntaxException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isValidBranch(String branch) {
		return !branch.equals(getGHRepository().getDefaultBranch());
	}

	public static void createBranch(String branch) throws InvalidBranchException, IOException, BranchAlreadyExistsException {
		if(!isValidBranch(branch))
			throw new InvalidBranchException();

		lockBranch(branch);
		try {			
			String latestCommitInMain = getLatestCommit(getGHRepository().getDefaultBranch());

			// não encontrei forma de criar branch com a biblioteca java
			// portanto vamos usar aqui a API REST diretamente
			
			String authorizationHeader = "Bearer " + getGithubAccessToken();
			
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

	public static String getDefaultBranch() {
		return getGHRepository().getDefaultBranch();
	}
}
