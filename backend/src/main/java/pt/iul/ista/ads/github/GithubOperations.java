package pt.iul.ista.ads.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.kohsuke.github.GHBlob;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContentUpdateResponse;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.HttpException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.iul.ista.ads.models.BranchResponseModel;
import pt.iul.ista.ads.owl.Ontology;
import pt.iul.ista.ads.owl.OntologyException;

public class GithubOperations extends GithubOperationsBase {

	private static ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();
	
	private static final String owlPath = "knowledge-base.owl";
	
	// retorna hash do commit efetuado
	public static String editOntology(String branch, String commit, boolean isCurator, OntologyEditorCallback callback) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		if(!isValidBranch(branch) && !isCurator)
			throw new InvalidBranchException();
		
		lockBranch(branch);
		try {
			checkIsLatestCommit(branch, commit);

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
	
	private static void checkIsLatestCommit(String branch, String commit) throws IOException, BranchNotFoundException, OldCommitException {
		String latestCommit = getLatestCommit(branch);
		if(!commit.equals(latestCommit))
			throw new OldCommitException(latestCommit, branch);
	}
	
	public static ReadOntologyResponse readOntology(String branch) throws IOException, OntologyException, BranchNotFoundException {
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
	
	private static GetOWLResponse getOWL(String branch) throws IOException, BranchNotFoundException {
		try {
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
		} catch(GHFileNotFoundException e) {
			throw new BranchNotFoundException(branch);
		}
	}
	
	private static String getOWLSha(String branch) throws IOException, BranchNotFoundException {
		try {
			GHTree tree = getGHRepository().getTree(branch);
			GHBlob owlBlob = null;
			for(GHTreeEntry entry : tree.getTree())
				if(entry.getPath().equals(owlPath))
					owlBlob = entry.asBlob();
			return owlBlob.getSha();
		} catch(GHFileNotFoundException e) {
			throw new BranchNotFoundException(branch);
		}
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
	
	public static String getLatestCommit(String branch) throws IOException, BranchNotFoundException {
		// versão original deste método, usando a biblioteca java:
		//return repository.getCommit(branch).getSHA1();
		// por vezes não funciona como desejado, por isso vamos usar a api REST diretamente
		return getLatestCommitObject(branch).get("sha").getAsString();
	}
	
	private static JsonObject getLatestCommitObject(String branch) throws BranchNotFoundException, IOException {
		String authorizationHeader = "Bearer " + getGithubAccessToken();

		Request request = new Request.Builder()
				.url("https://api.github.com/repos/ads-meipl/knowledge-base/commits/" + branch)
				.addHeader("Authorization", authorizationHeader)
				.build();
		Call call = client.newCall(request);
		Response response = call.execute();
		
		if(response.code() == 422)
			throw new BranchNotFoundException(branch);
		
		JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
		return responseObject;
	}
	
	public static boolean isValidBranch(String branch) {
		return !branch.equals(getGHRepository().getDefaultBranch());
	}

	public static void createBranch(String branch) throws InvalidBranchException, IOException, BranchAlreadyExistsException {
		checkIsValidBranch(branch);

		lockBranch(branch);
		try {
			createBranchImpl(branch);
		} finally {
			unlockBranch(branch);
		}
		
	}
	
	// método sem lock; necessário fazer lock antes de chamar este método
	private static void createBranchImpl(String branch) throws IOException, InvalidBranchException, BranchAlreadyExistsException {
		String latestCommitInMain;
		try {
			latestCommitInMain = getLatestCommit(getGHRepository().getDefaultBranch());
		} catch(BranchNotFoundException e) {
			// não é suposto o fluxo chegar aqui
			throw new RuntimeException(e);
		}
		
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
	}

	public static String getDefaultBranch() {
		return getGHRepository().getDefaultBranch();
	}
	
	public static List<BranchResponseModel> listBranches() throws IOException {
		List<BranchResponseModel> res = new ArrayList<BranchResponseModel>();
		try {
			for(String branchName : getGHRepository().getBranches().keySet()) {
				String date = getLatestCommitObject(branchName)
						.get("commit").getAsJsonObject()
						.get("author").getAsJsonObject()
						.get("date").getAsString();
				BranchResponseModel branchModel = new BranchResponseModel();
				branchModel.setBranchName(branchName);
				branchModel.setLastCommitDate(date);
				res.add(branchModel);
			}
		} catch(BranchNotFoundException e) {
			throw new RuntimeException(e);
		}
		return res;
	}
	
	public static void mergeBranch(String branchName, String commit) throws IOException, BranchNotFoundException, OldCommitException, InvalidBranchException, MergeConflictException {
		checkIsValidBranch(branchName);
		lockBranch(branchName);
		try {
			checkIsLatestCommit(branchName, commit);
			GHBranch branch = getGHRepository().getBranch(branchName);
			getGHRepository().getBranch(getDefaultBranch()).merge(branch, "Merge branch " + branchName);
			deleteBranchImpl(branchName);
		} catch(HttpException e) {
			if(e.getResponseCode() == 409)
				throw new MergeConflictException();
			else
				throw e;
		} finally {
			unlockBranch(branchName);
		}
	}
	
	private static void checkIsValidBranch(String branch) throws InvalidBranchException {
		if(!isValidBranch(branch))
			throw new InvalidBranchException();
	}
	
	public static void deleteBranch(String branchName, String commit) throws IOException, BranchNotFoundException, OldCommitException, InvalidBranchException {
		checkIsValidBranch(branchName);
		lockBranch(branchName);
		try {
			checkIsLatestCommit(branchName, commit);
			deleteBranchImpl(branchName);
		} finally {
			unlockBranch(branchName);
		}
	}
	
	// método sem lock; necessário fazer lock antes de chamar este método
	private static void deleteBranchImpl(String branchName) throws IOException {		
		String authorizationHeader = "Bearer " + getGithubAccessToken();
		Request request = new Request.Builder()
				.url("https://api.github.com/repos/ads-meipl/knowledge-base/git/refs/heads/" + branchName)
				.addHeader("Authorization", authorizationHeader)
				.delete()
				.build();
		Call call = client.newCall(request);
		call.execute();
	}
	
	public static void mergeBranchOwl(String branchName, String commit, String owl) throws IOException, BranchNotFoundException, OldCommitException, OntologyException, InvalidBranchException {
		checkIsValidBranch(branchName);
		lockBranch(branchName);
		try {
			checkIsLatestCommit(branchName, commit);
			
			// lança OntologyException se owl é inválido
			new Ontology(owl);
			
			String owlSha = getOWLSha(getDefaultBranch());
			getGHRepository().createContent()
					.branch(getDefaultBranch())
					.path(owlPath)
					.content(owl)
					.message("Merge branch " + branchName)
					.sha(owlSha)
					.commit();
			deleteBranchImpl(branchName);
		} finally {
			unlockBranch(branchName);
		}
	}
	
	public static String getBranchOwl(String branchName) throws IOException, BranchNotFoundException {
		return getOWL(branchName).owl;
	}
	
	public static void syncBranch(String branchName, String commit) throws IOException, BranchNotFoundException, InvalidBranchException, OldCommitException {
		checkIsValidBranch(branchName);
		lockBranch(branchName);
		try {
			checkIsLatestCommit(branchName, commit);
			
			deleteBranchImpl(branchName);
			createBranchImpl(branchName);
		} catch(BranchAlreadyExistsException e) {
			// não é suposto ser possível chegar a este ponto
			throw new RuntimeException(e);
		} finally {
			unlockBranch(branchName);
		}
	}
}
