package pt.iul.ista.ads.github;

import java.io.IOException;
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

import pt.iul.ista.ads.owl.Ontology;
import pt.iul.ista.ads.owl.OntologyException;

public class GithubOperations {

	private static ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();
	
	private static GHRepository repository;
	
	private static final String owlPath = "knowledge-base.owl";
	
	private static Logger logger = LoggerFactory.getLogger(GithubOperations.class);
	
	static {
		try {
			String token;
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
		lockBranch(branch);
		try {
			// check if branch is valid
			if(branch.equals(repository.getDefaultBranch()))
				throw new InvalidBranchException();
			
			// check if commit is the latest commit
			String latestCommit = getLatestCommit(branch);
			if(!commit.equals(latestCommit))
				throw new OldCommitException(latestCommit);
			
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
		if(!locks.containsKey(branch))
			locks.put(branch, new ReentrantLock());
		locks.get(branch).lock();
	}
	
	private static void unlockBranch(String branch) {
		locks.get(branch).unlock();
	}
	
	public static String getLatestCommit(String branch) throws IOException {
		return repository.getCommit(branch).getSHA1();
	}
}
