package com.PixelApi.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.PixelApi.Entity.Client;
import com.PixelApi.Entity.ClientEmail;
import com.PixelApi.Entity.FreeSubscription;
import com.PixelApi.Entity.StripeSubscription;
import com.PixelApi.Entity.StripeSubscriptionDto;
import com.PixelApi.Repository.ClientEmailRepo;
import com.PixelApi.Repository.ClientRepo;
import com.PixelApi.Repository.FreeSubscriptionRepo;
import com.PixelApi.Repository.StripeSubscriptionRepo;
import com.PixelApi.Security.Jwt;
import com.PixelApi.Util.SendMail;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClientService {

	@Autowired
	private final Jwt myJwt = new Jwt();

	@Autowired
	SendMail sendMailClass;

	@Autowired
	ClientEmailRepo clientEmailRepo;

	@Autowired
	ClientRepo clientRepo;

	@Autowired
	StripeSubscriptionRepo stripeRepo;

	@Autowired
	FreeSubscriptionRepo freeRepo;

	@Value("${stripe.public}")
	private String stripePublic;

	@Value("${stripe.secret}")
	private String stripeSecret;

	@Value("${stripe.premium}")
	private String stripePremium;

	@Value("${stripe.premium_plus}")
	private String stripePremiumPlus;

	@Transactional
	public Map<String, String> StripeCredentials(String plan) {

		log.info("Inside StripeCredentials method");
		Map<String, String> response = new HashMap<>();

		try {
			response.put("STRIPE_PUBLIC", stripePublic);
			response.put("STRIPE_SECRET", stripeSecret);
			String planCode = plan.equals("premium") ? stripePremium : stripePremiumPlus;
			response.put("STRIPE_PLAN_CODE", planCode);

		} catch (Exception e) {
			log.error("Internal error in save method: " + e);
			response.put("code", "666");
			response.put("internal error in confirmAccount", e.toString());
		}
		return response;
	}

	@Transactional
	public FreeSubscription createFreeSubscription(String email) {

		log.info("Inside CreateFreeSubscription method");
		FreeSubscription freeSubscription = null;

		try {

			Timestamp startDay = new Timestamp(System.currentTimeMillis());
			Timestamp endDay = Timestamp.valueOf(startDay.toLocalDateTime().plusYears(100));

			Client myclient = clientRepo.findByEmail(email);
			String token = myJwt.create(myclient.getClientId().toString(), startDay, endDay, "Free");

			freeSubscription = new FreeSubscription(token, myclient.getClientId(), startDay);

			freeRepo.save(freeSubscription);
			log.info("StripeSubscription has been saved");

		} catch (Exception e) {
			log.error("Internal error in save method: " + e);
		}
		return freeSubscription;
	}

	@Transactional
	public StripeSubscription createStripeSubscription(StripeSubscriptionDto mySubscription) {

		log.info("Inside CreateStripeSubscription method");
		StripeSubscription stripeSubscription = null;

		try {

			Timestamp startDay = new Timestamp(System.currentTimeMillis());
			Timestamp endDay = Timestamp.valueOf(startDay.toLocalDateTime().plusMonths(1));

			Client myclient = clientRepo.findByEmail(mySubscription.getEmail());
			String token = myJwt.create(myclient.getClientId().toString(), startDay, endDay, "Premium");

			stripeSubscription = new StripeSubscription(mySubscription.getStripeSubscriptionId(), token,
					myclient.getClientId(), startDay, endDay, true, 1);

			stripeRepo.save(stripeSubscription);
			log.info("StripeSubscription has been saved");

			new Thread(() -> {
				// CREATE TOKEN AND SEND EMAIL

				log.info("Sending email to: " + mySubscription.getEmail());
				sendMailClass.sendMail(myclient.getEmail(), token, "PREMIUM_ACCOUNT");
				log.info("Email has been sent");

				// SEND INVOICE HOW?

			}).start();

		} catch (Exception e) {
			log.error("Internal error in save method: " + e);
		}
		return stripeSubscription;
	}

	public Map<String, Object> Update(Client myClient) {

		Map<String, Object> response = new HashMap<>();
		log.info("Inside update method");

		Client updateClient = clientRepo.findById(myClient.getClientId()).get();
		updateClient.setEmail(myClient.getEmail());
		updateClient.setAcctKey(new BCryptPasswordEncoder().encode(myClient.getAcctKey()));
		response.put("Updated client", clientRepo.save(updateClient));

		return response;
	}

	public Map<String, Object> Login(String email, String accountKey) {

		log.info("Inside Login method");
		FreeSubscription freeSubscription = null;
		Map<String, Object> response = new HashMap<>();

		Client myClient = clientRepo.findByEmail(email);
		Boolean answer = new BCryptPasswordEncoder().matches(accountKey, myClient.getAcctKey());

		if (answer == false) {
			response.put("response", "Not found");
		} else {
			response.put("myClient", myClient);
			freeSubscription = freeRepo.findByClientId(myClient.getClientId());
			if (freeSubscription != null) {
				response.put("token", freeSubscription);
			} else {
				response.put("token", null);
			}

		}

		return response;
	}

	public Map<String, String> ValidateAccount(String email) {

		log.info("Inside validateAccount method");
		Map<String, String> response = new HashMap<>();

		if (clientRepo.countByEmail(email) == 0) {
			log.info("New account");
			response.put("message", "No existing record given that email");
			response.put("code", "A");
			response.put("codeExplanation", "This is a new account");
		} else {

			Client myClient = clientRepo.findByEmail(email);

			if (clientEmailRepo.tokenValidation(myClient.getClientId(), "ACCT_CONFIRMATION", true) == 1) {
				log.info("Existing user trying to login, let's validate now the password ");
				response.put("message", "Existing email, let's validate password");
				response.put("code", "B");
				response.put("codeExplanation", "Existing functional account");
				response.put("id", myClient.getClientId().toString());
			} else {
				response.put("message", "Existing record given that email, but not activated");
				response.put("code", "C");
				response.put("codeExplanation", "Account hasn't been activated");
				response.put("additionalField", myClient.getClientId().toString() + "," + myClient.getAcctKey());
				log.info("Account hasn't been activated");
			}
		}
		return response;
	}

	@Transactional
	public Client Save(Client myClient) {

		Client myResponse = null;
		Boolean sendMail = false;

		log.info("Inside save method");

		try {
			String email = myClient.getEmail();
			String confirmationCode = UUID.randomUUID().toString();

			/*
			 * SEND CONFIRMATION EMAIL
			 */

			if (clientRepo.countByEmail(email) == 0) {
				String encryptedPwd = new BCryptPasswordEncoder().encode(myClient.getAcctKey());
				myClient.setAcctKey(encryptedPwd);
				myResponse = clientRepo.save(myClient);
				sendMail = true;
				log.info("Client saved");

				clientEmailRepo.save(
						new ClientEmail(confirmationCode, myResponse.getClientId(), null, "ACCT_CONFIRMATION", false));
				log.info("Confirmation token saved");

				new Thread(() -> {
					log.info("Sending email to: " + email);
					sendMailClass.sendMail(email, confirmationCode, "CONFIRMATION_ACCOUNT");
					log.info("Email has been sent");
				}).start();

			} else {

				/*
				 * DON'T SENT CONFIRMATION EMAIL
				 */
				Client existingClient = clientRepo.findByEmail(email);
				existingClient.setAcctKey(new BCryptPasswordEncoder().encode(myClient.getAcctKey()));
				myResponse = clientRepo.save(existingClient);
				log.info("Client just changed pwd xd in the modal flow");
			}

			return myResponse;

		} catch (Exception e) {
			log.error("Internal error in save method: " + e);
			return null;
		}
	}

	@Transactional
	public Map<String, Object> ForgotPwd(String email) {

		Map<String, Object> response = new HashMap<>();
		log.info("Inside ForgotPwd method");

		try {

			Long id = clientRepo.findByEmail(email).getClientId();
			Long xd = clientEmailRepo.tokenValidation(id, "RECOVER_PWD", false);

			if (clientEmailRepo.tokenValidation(id, "RECOVER_PWD", false) == 0) {

				String confirmationCode = UUID.randomUUID().toString();
				clientEmailRepo.save(new ClientEmail(confirmationCode, id, null, "RECOVER_PWD", false));

				log.info("Client token saved");
				response.put("id", id.toString());
				response.put("token",freeRepo.findByClientId(id));
				
				response.put("message", "Email has been sent :)");

				new Thread(() -> {
					log.info("Sending email to: " + email);
					sendMailClass.sendMail(email, confirmationCode, "RECOVER_ACCOUNT");
					log.info("Email has been sent");

				}).start();
			} else {
				response.put("value", id.toString());
				response.put("message", "Email has already been sent :P");
			}

			return response;

		} catch (Exception e) {
			log.error("Internal error in save method: " + e);
			return null;
		}
	}

	@Transactional
	public Map<String, String> ConfirmAccount(String token) {

		log.info("Inside confirmAccount method");
		Map<String, String> response = new HashMap<>();

		try {
			ClientEmail myToken = clientEmailRepo.findById(token).orElse(null);

			if (myToken == null) {
				log.info("Invalid Token");
				response.put("code", "BB");
				response.put("message", "Invalid token");
			} else {

				myToken.setUsed(true);
				clientEmailRepo.save(myToken);
				log.info("Token has beed confirmed");
				log.info("Token reason: " + myToken.getReason());
				response.put("code", "AA");
				response.put("message", "Token confirmed");
			}

		} catch (Exception e) {
			log.error("Some problem occured in save method: " + e);
			response.put("code", "666");
			response.put("internal error in confirmAccount", e.toString());
		}

		return response;
	}

	public Client FindUserbyUsername(String user) {
		Client myClient = clientRepo.findById(1L).get();
		return myClient;
	}
}