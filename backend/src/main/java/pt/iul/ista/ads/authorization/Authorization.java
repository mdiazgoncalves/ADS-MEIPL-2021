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
	
	public static String curatorIssuer = "curator";
	
	private static final String curatorPassword = "banana";
	
	static {
		try {
			signer = RSASigner.newSHA256Signer(Utils.resourceToString(privateKeyFilename));
			verifier = RSAVerifier.newVerifier(Utils.resourceToString(publicKeyFilename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String generateToken(String password) throws UnauthorizedException {
		if(!password.equals(curatorPassword))
			throw new UnauthorizedException();
		
		JWT jwt = new JWT()
				.setIssuer(curatorIssuer)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusHours(24));
		return JWT.getEncoder().encode(jwt, signer);
	}
	
	public static void checkValidToken(String token) throws UnauthorizedException {
		if(!isValidToken(token))
			throw new UnauthorizedException();
	}

	public static boolean isValidToken(String token) {
		if(token == null)
			return false;
		JWT jwt = JWT.getDecoder().decode(token, verifier);
		return jwt.issuer.equals(curatorIssuer) && !jwt.isExpired();
			
	}
}
