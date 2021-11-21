package pt.iul.ista.ads.authorization;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import io.fusionauth.jwt.rsa.RSAVerifier;
import pt.iul.ista.ads.utils.Utils;

public class Authorization {
	
	private static final String privateKeyFilename = "backend_private_key.pem";
	
	private static final String publicKeyFilename = "backend_public_key.pem";
	
	private static Signer signer;
	
	private static Verifier verifier;
	
	static {
		try {
			signer = RSASigner.newSHA256Signer(Utils.resourceToString(privateKeyFilename));
			verifier = RSAVerifier.newVerifier(Utils.resourceToString(publicKeyFilename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String generateEditorToken(String branch) {		
		JWT jwt = new JWT()
				.setIssuer(branch)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC));
		// TODO estamos a criar um jwt que não expira. queremos manter este comportamento?

		return JWT.getEncoder().encode(jwt, signer);
	}
	
	public static void checkValidEditor(String branch, String token) throws UnauthorizedException {
		JWT jwt = JWT.getDecoder().decode(token, verifier);
		if(!jwt.issuer.equals(branch))
			throw new UnauthorizedException();
	}

}
