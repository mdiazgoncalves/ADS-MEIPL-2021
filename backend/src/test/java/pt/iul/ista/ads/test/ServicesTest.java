package pt.iul.ista.ads.test;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import pt.iul.ista.ads.github.BranchNotFoundException;
import pt.iul.ista.ads.github.GithubOperations;
import pt.iul.ista.ads.github.InvalidBranchException;
import pt.iul.ista.ads.github.OldCommitException;
import pt.iul.ista.ads.heroku.Main;
import pt.iul.ista.ads.models.ClassesResponseModel;
import pt.iul.ista.ads.models.LatestCommitResponseModel;


public class ServicesTest extends JerseyTest {
	
	@Override
	protected Application configure() {
		try {
			Main.init();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages("pt.iul.ista.ads.services", "pt.iul.ista.ads.exceptionmappers");
		return resourceConfig;
	}
	
	// Os testes assumem que o as classes definidas aqui não existem
	public static final String branch = "unittests@example.com";
	public static final String testClass = "UnitTestClass";
	public static final String testClass2 = "UnitTestClass2";

	private void resetBranch() {
		try {
			String latestCommit = GithubOperations.getLatestCommit("unittests@example.com");
			GithubOperations.deleteBranch(branch, latestCommit);
		} catch (BranchNotFoundException e) {
			// ignorar
		} catch (OldCommitException | InvalidBranchException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void test() {
		resetBranch();
		
		// criar branch
		Response response = target("/branch/" + branch).request().post(Entity.json(""));
		assertEquals(200, response.getStatus());
		
		// obter último commit
		ClassesResponseModel classesResponseModel = target("/classes").queryParam("branch", branch).request().get(ClassesResponseModel.class);
		String latestCommit = classesResponseModel.getLatestCommit();
		assertNotNull(latestCommit);
		
		// criar classe
		response = target("/class/" + testClass).queryParam("branch", branch).queryParam("commit", latestCommit).request().post(Entity.json("{\"superClassName\": null}"));
		assertEquals(200, response.getStatus());
		String oldCommit = latestCommit;
		latestCommit = response.readEntity(LatestCommitResponseModel.class).getLatestCommit();
		
		// verificar se classe foi criada
		response = target("/class/" + testClass).queryParam("branch", branch).request().get();
		assertEquals(200, response.getStatus());
		
		// verificar se o serviço deteta conflito ao usar commit antigo
		// usamos testClass2 porque não queremos que o erro seja devido a criar uma classe que já existe
		response = target("/class/" + testClass2).queryParam("branch", branch).queryParam("commit", oldCommit).request().post(Entity.json("{\"superClassName\": null}"));
		assertEquals(409, response.getStatus());
		
		// verificar se o serviço deteta que a classe já existe
		response = target("/class/" + testClass).queryParam("branch", branch).queryParam("commit", latestCommit).request().post(Entity.json("{\"superClassName\": null}"));
		assertEquals(400, response.getStatus());
		
		// apagar classe
		response = target("/class/" + testClass).queryParam("branch", branch).queryParam("commit", latestCommit).request().delete();
		assertEquals(200, response.getStatus());
		latestCommit = response.readEntity(LatestCommitResponseModel.class).getLatestCommit();

		// login do curador
		response = target("/login/curator").queryParam("password", "banana").request().post(Entity.json(""));
		assertEquals(200, response.getStatus());
		String token = response.readEntity(String.class);
		
		// apagar branch
		response = target("/branch/" + branch).queryParam("commit", latestCommit).queryParam("token", token).request().delete();
		assertEquals(200, response.getStatus());
	}
	
	
}
