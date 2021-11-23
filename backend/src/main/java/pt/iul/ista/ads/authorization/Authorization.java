package pt.iul.ista.ads.authorization;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import io.fusionauth.jwt.rsa.RSAVerifier;
import pt.iul.ista.ads.github.GithubOperations;
import pt.iul.ista.ads.utils.Utils;

public class Authorization {
	
	private static final String privateKeyFilename = "backend_private_key.pem";
	
	private static final String publicKeyFilename = "backend_public_key.pem";
	
	private static Signer signer;
	
	private static Verifier verifier;
	
	public static String curatorIssuer = "curator";
	
	static {
		try {
			signer = RSASigner.newSHA256Signer(Utils.resourceToString(privateKeyFilename));
			verifier = RSAVerifier.newVerifier(Utils.resourceToString(publicKeyFilename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public enum OperationType {
		READ, EDIT
	}

	public static String generateEditorToken(String branch) {		
		JWT jwt = new JWT()
				.setIssuer(branch)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC));

		return JWT.getEncoder().encode(jwt, signer);
	}
	
	public static String generateCuratorToken() {
		JWT jwt = new JWT()
				.setIssuer(curatorIssuer)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusHours(24));
		return JWT.getEncoder().encode(jwt, signer);
	}
	
	public static void checkValidToken(String branch, String token, OperationType type) throws UnauthorizedException {
		if(branch.equals(GithubOperations.getDefaultBranch()) && type == OperationType.READ)
			return;

		JWT jwt = JWT.getDecoder().decode(token, verifier);
		if(!(jwt.issuer.equals(branch) || (jwt.issuer.equals(curatorIssuer) && !jwt.isExpired())))
			throw new UnauthorizedException();
		// TODO para o caso do editor a minha ideia era verificar o isseudAt para comparar
		// a data de emissão do token com a data de criação do branch.
		// Se foi issued depois da criação do branch, é rejeitado
		// Problema: não há operação na API do github para ver data de criação do branch
		// Ideia: manter no repositório um ficheiro que mapeia nome do branch à sua data de criação
		// Podemos ter um branch específico dedicado apenas a esse ficheiro
	}

}
