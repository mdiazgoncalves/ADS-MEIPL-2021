package pt.iul.ista.ads.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import pt.iul.ista.ads.utils.Utils;

class GithubOperationsBase {

	private static final String keyFilename = "ads-meipl.2021-11-18.private-key.pem";
	private static final String appId = "152903";
	private static final int maxJWTDurationMinutes = 10;
	private static final String githubApiUrl = "https://api.github.com/integration/installations";
	
	private static String token;
	private static ZonedDateTime token_expires_at;
	
	private static GHRepository repository;
	
	private static boolean shouldRefresh() {
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		return token == null || now.isAfter(token_expires_at.minusMinutes(10));
	}
	
	protected static String getGithubAccessToken() {
		if(shouldRefresh())
			generateGithubAccessToken();
		return token;
	}
	
	protected static GHRepository getGHRepository() {
		if(shouldRefresh())
			generateGithubAccessToken();
		return repository;
	}
	
	private static void generateGithubAccessToken() {
		// O código inteiro está dentro de um try porque nenhuma das potenciais exceções
		// podem ser lançadas em circunstâncias normais.
		// Se alguma dessas exceções forem lançadas, é sinal que será preciso alterar
		// o código desta classe, pois alguma coisa na api do github mudou.
		try {
			String jwt = generateJWT();
			String authorizationHeader = "Bearer " + jwt;
			
			// get access tokens URL
			HttpClient client = HttpClient.newBuilder()
					.followRedirects(Redirect.NORMAL)
					.version(Version.HTTP_1_1)
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(githubApiUrl))
					.setHeader("Authorization", authorizationHeader)
					.build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			
			JsonArray responseArray = JsonParser.parseString(response.body()).getAsJsonArray();
			JsonObject responseObject = responseArray.get(0).getAsJsonObject();
			String accessTokensUrl = responseObject.get("access_tokens_url").getAsString();
			
			// get access token
			request = HttpRequest.newBuilder()
					.uri(new URI(accessTokensUrl))
					.setHeader("Authorization", authorizationHeader)
					.POST(BodyPublishers.noBody())
					.build();
			response = client.send(request, BodyHandlers.ofString());
			
			responseObject = JsonParser.parseString(response.body()).getAsJsonObject(); 

			token = responseObject.get("token").getAsString();
			token_expires_at = ZonedDateTime.parse(responseObject.get("expires_at").getAsString());
			
			GitHub github = new GitHubBuilder().withAppInstallationToken(token).build();
			repository = github.getRepository("ads-meipl/knowledge-base");
			
		} catch(InterruptedException | IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String generateJWT() throws IOException {
		String keyFileContent = Utils.resourceToString(keyFilename);
		Signer signer = RSASigner.newSHA256Signer(keyFileContent);
		JWT jwt = new JWT()
				.setIssuer(appId)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(maxJWTDurationMinutes));
		return JWT.getEncoder().encode(jwt, signer);
	}
	
}
