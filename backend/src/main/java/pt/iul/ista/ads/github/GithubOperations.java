package pt.iul.ista.ads.github;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.kohsuke.github.GHBlob;
import org.kohsuke.github.GHContentUpdateResponse;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
		String authorizationHeader = "Bearer " + getGithubAccessToken();

		Request request = new Request.Builder()
				.url("https://api.github.com/repos/ads-meipl/knowledge-base/commits/" + branch)
				.addHeader("Authorization", authorizationHeader)
				.build();
		Call call = client.newCall(request);
		Response response = call.execute();
		
		JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
		return responseObject.get("sha").getAsString();
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
			
			JsonObject bodyObject = new JsonObject();
			bodyObject.addProperty("ref", "refs/heads/" + branch);
			bodyObject.addProperty("sha", latestCommitInMain);
			String body = bodyObject.toString();
			
			RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json"));
			Request request = new Request.Builder()
					.url("https://api.github.com/repos/ads-meipl/knowledge-base/git/refs")
					.addHeader("Authorization", authorizationHeader)
					.post(requestBody)
					.build();
			Call call = client.newCall(request);
			Response response = call.execute();
			
			if(response.code() == 422) {
				JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
				String message = responseObject.get("message").getAsString();
				if(message.contains("is not a valid ref name")) {
					throw new InvalidBranchException();
				} else if(message.contains("Reference already exists")) {
					throw new BranchAlreadyExistsException();
				} else {
					throw new RuntimeException(message);
				}
			}
		} finally {
			unlockBranch(branch);
		}
		
	}

	public static String getDefaultBranch() {
		return getGHRepository().getDefaultBranch();
	}
}
