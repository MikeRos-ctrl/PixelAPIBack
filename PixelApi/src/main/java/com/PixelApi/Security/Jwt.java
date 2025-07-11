package com.PixelApi.Security;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Component
public class Jwt {

	private static String SECRET_KEY = "LindaNoceloEsElAmorDeMiVida<3";
	private static Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
	
	public String create(String username, Timestamp creation, Timestamp expiration, String subscriptionType) {
		return JWT.create()
				.withSubject(username)
				.withIssuer("Created By ThePixelApiTeam")
				.withClaim("subscriptionType", subscriptionType)
				.withIssuedAt(creation)
	            .withExpiresAt(expiration)
				.sign(Algorithm.HMAC256(SECRET_KEY));
	}
	
	public String getSubscriptionType(String jwt) {
	    return JWT.require(ALGORITHM)
	            .build()
	            .verify(jwt)
	            .getClaim("subscriptionType")
	            .asString();
	}
	
	public boolean isValid(String jwt) {

		try {		
			JWT
			.require(ALGORITHM)
			.build()
			.verify(jwt);
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}
	}
	
	public String getUsername(String jwt) {
		
		return JWT.require(ALGORITHM)
				.build()
				.verify(jwt)
				.getSubject();
	}
}