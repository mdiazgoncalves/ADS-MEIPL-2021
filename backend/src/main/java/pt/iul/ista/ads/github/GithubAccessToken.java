package pt.iul.ista.ads.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import pt.iul.ista.ads.utils.Utils;

// TODO cache e refresh automático do access token
public class GithubAccessToken {

	private static final String keyFilename = "ads-meipl.2021-11-18.private-key.pem";
	private static final String appId = "152903";
	private static final int maxJWTDurationMinutes = 10;
	private static final String githubApiUrl = "https://api.github.com/integration/installations";
	
	public static String getGithubAccessToken() {
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
			
			String token = JsonParser.parseString(response.body()).getAsJsonObject().get("token").getAsString();
			
			return token;
		} catch(Exception e) {
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
