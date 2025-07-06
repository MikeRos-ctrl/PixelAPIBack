package com.PixelApi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.PixelApi.Entity.Image;
import com.PixelApi.Entity.ImageCategoryDTO;
import com.PixelApi.Entity.StripeSubscriptionDto;
import com.PixelApi.Entity.CategoryImage;
import com.PixelApi.Entity.Client;
import com.PixelApi.Service.ClientService;
import com.PixelApi.Service.ImageService;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Storage.SignUrlOption;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.springframework.web.bind.annotation.CrossOrigin;

@Slf4j
@RestController
@RequestMapping("/frontController")
public class FrontController {

	@Autowired
	ImageService service;

	@Autowired
	ClientService myClientService;

	@Value("${image.link}")
	private String imageLink;
	
	private static final String BUCKET_NAME = "aestheticpixelart";
	private static final String SERVICE_ACCOUNT_JSON = "/etc/secrets/pixelapikey.json";
	private Storage storage;

	public FrontController() throws Exception {
		this.storage = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_JSON))).build()
				.getService();
	}

	@GetMapping("/stripeCredentials")
	public ResponseEntity<?> StripeCredentials(@RequestParam("plan") String plan) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			response.put("response", myClientService.StripeCredentials(plan));
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in StripeCredentials: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@PostMapping("/stripeSubscription")
	public ResponseEntity<?> createStripeSubscription(@RequestBody StripeSubscriptionDto data) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			response.put("response", myClientService.createStripeSubscription(data));
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in stripeSubscription: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}
	
	@PostMapping("/freeSubscription")
	public ResponseEntity<?> createFreeSubscription(@RequestParam("email") String email) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			response.put("response", myClientService.createFreeSubscription(email));
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in stripeSubscription: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@GetMapping("/validateAccount/{email}")
	public ResponseEntity<?> validateAccount(@PathVariable String email) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			response.put("response", myClientService.ValidateAccount(email));
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in validateAccount: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}
	
	@PostMapping("/createAccount")
	public ResponseEntity<?> createAccount(@Valid @RequestBody Client myClient, BindingResult result) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		if (result.hasErrors()) {
			List<String> errors = new ArrayList<String>();

			for (FieldError err : result.getFieldErrors()) {
				errors.add("Campo '" + err.getField() + "' " + err.getDefaultMessage());
			}

			response.put("errors", errors);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			response.put("value", myClientService.Save(myClient));
			response.put("mensaje", "Usuario creado exitosamente");
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in createAccount: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@PostMapping("/confirmAccount/{token}")
	public ResponseEntity<?> confirmAccount(@PathVariable String token) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			
			statusResponse = (myClientService.ConfirmAccount(token).get("code").equals("BB")) 
					? HttpStatus.BAD_REQUEST : HttpStatus.OK;
			
			response.put("response", myClientService.ConfirmAccount(token));

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in confirmAccount: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@GetMapping("/forgotPwd/{email}")
	public ResponseEntity<?> forgotPwd(@PathVariable String email) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			response = myClientService.ForgotPwd(email);
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in forgotPwd: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@GetMapping("/login/{email}/{accountKey}")
	public ResponseEntity<?> login(@PathVariable String email, @PathVariable String accountKey) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
	
			 response = myClientService.Login(email, accountKey);

			if (response != null) {
				statusResponse = HttpStatus.OK;
			} else {
				statusResponse = HttpStatus.NOT_FOUND;
			}

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in login: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@PutMapping("/updateAccount")
	public ResponseEntity<?> updateAccount(@RequestBody Client myClient) {

		Map<String, Object> response = new HashMap<>();
		HttpStatus statusResponse = HttpStatus.OK;

		try {
			response.put("response", myClientService.Update(myClient));
			statusResponse = HttpStatus.OK;

		} catch (Exception e) {
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in updateAccount: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(response, statusResponse);
	}

	@GetMapping("/getRandomImageWithCategories")
	public ResponseEntity<?> GetRandomImageWithCategories() {

		HttpStatus statusResponse = HttpStatus.OK;
		List<Map<String, Object>> myResponse = new ArrayList<>();
		
		try {
			ImageCategoryDTO myImage = service.getRandomImageWithCategories();	
			Map<String, Object> formatedDto = Map.of(
					"Image", "src\\assets\\\\PixelArt\\" + myImage.getImageId(),
					"Name", myImage.getName(),
					"ImageId", myImage.getImageId(),
					"Categories", myImage.getCategoryNames(),
					"Description", myImage.getDescription()
					);
			myResponse.add(formatedDto);
			return new ResponseEntity<>(myResponse, HttpStatus.OK);
			
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			myResponse.add(response);
			log.error("error in GetRandomImageWithCategories: " + e.getMessage());
			statusResponse = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(myResponse, statusResponse);
	}

	@GetMapping("/fillFrontHeader")
	public ResponseEntity<?> fillFrontHeader() {

		HttpStatus statusResponse = HttpStatus.OK;
		List<Map<String, Object>> myResponse = new ArrayList<>();

		try {
			List<Image> myList = service.FillFront();

			for (Image element : myList) {

				Map<String, Object> response = new HashMap<>();				
				response.put("Image", "public_html/assets/PixelArt/" + element.getImageId());
				response.put("Name", element.getName());
				response.put("ImageId", element.getImageId());
				myResponse.add(response);
			}

			return new ResponseEntity<>(myResponse, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			myResponse.add(response);
			log.error("error in fillFrontHeader: " + e.getMessage());
			return new ResponseEntity<>(myResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/listByCategory/{category}/{imageId}")
	public ResponseEntity<?> listByCategory(
			@PathVariable int category,
			@PathVariable String imageId) {

		HttpStatus statusResponse = HttpStatus.OK;
		List<Map<String, Object>> myList = new ArrayList<>();

		try {

			List<CategoryImage> myImages = (category == 8) ? myImages = service.getImagesByCategory2(imageId)
					: service.getImagesByCategory(category, imageId);

			for (CategoryImage element : myImages) {
				Map<String, Object> response = new HashMap<>();
				response.put("Image", "src\\assets\\\\PixelArt\\" + element.getImageId());
				response.put("Name", service.findNameById(element.getImageId()));
				response.put("ImageId", element.getImageId());
				myList.add(response);
			}

			return new ResponseEntity<>(myList, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			log.error("error in listByCategory: " + e.getMessage());
			myList.add(response);
			return new ResponseEntity<>(myList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}