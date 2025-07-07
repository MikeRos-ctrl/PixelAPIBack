package com.PixelApi.Controller;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PixelApi.Service.ClientService;
import com.PixelApi.Service.ImageService;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Storage.SignUrlOption;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URL;

@RestController
@RequestMapping("/pixelapi")
public class Api {

	@Autowired
	ClientService myClientService;

	@Autowired
	ImageService service;

	@Autowired HttpServletRequest request;
	
	private static final String BUCKET_NAME = "aestheticpixelart";
	private static final String SERVICE_ACCOUNT_JSON = "/etc/secrets/pixelapikey.json";
	private Storage storage;

	public Api() throws Exception {
		this.storage = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_JSON))).build()
				.getService();
	}

	/*
	 * FETCH NUMBER OF ORDERED IMAGES number= 0 -> ALL number=# -> #
	 */
	@GetMapping("/getOrderedImages")
	public ResponseEntity<?> getOrderedImages(@RequestParam("number") String number) {

		HttpStatus statusResponse = HttpStatus.OK;
		List<Map<String, Object>> myResponse = new ArrayList<>();

		try {
			List<Object[]> myList = service.getOrderedImages(number);

			for (Object[] element : myList) {
				Map<String, Object> response = new HashMap<>();
				BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, element[0].toString()).build();
				URL signedUrl = storage.signUrl(blobInfo, 3, TimeUnit.MINUTES, SignUrlOption.withV4Signature());

				response.put("Image", signedUrl.toString());
				response.put("ImageId", element[0]);
				response.put("Name", element[1]);
				response.put("Description", element[2]);
				response.put("Categories", element[3]);
				myResponse.add(response);
			}
			return new ResponseEntity<>(myResponse, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();

			response.put("Message", "Inner Error");
			response.put("Error", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * FETCH NUMBER OF ORDERED IMAGES number=0 -> ALL number=# -> #
	 */
	@GetMapping("/getDisorderedImages")
	public ResponseEntity<?> getDisorderedImages(@RequestParam("number") String number) {

		List<Map<String, Object>> myResponse = new ArrayList<>();

		try {
			List<Object[]> myList = service.getRandomImages(number);

			for (Object[] element : myList) {
				Map<String, Object> response = new HashMap<>();
				BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, element[0].toString()).build();
				URL signedUrl = storage.signUrl(blobInfo, 3, TimeUnit.MINUTES, SignUrlOption.withV4Signature());

				response.put("Image", signedUrl.toString());
				response.put("ImageId", element[0]);
				response.put("Name", element[1]);
				response.put("Description", element[2]);
				response.put("Categories", element[3]);

				myResponse.add(response);
			}

			return ResponseEntity.ok(myResponse);

		} catch (Exception e) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("Message", "Inner Error");
			errorResponse.put("Error", "Invalid input data in number field");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/*
	 * FETCH IMAGES BY ID, RECEIVES A STRING LIST
	 */
	@GetMapping("/getImagesById")
	public ResponseEntity<?> getImagesById(@RequestBody List<String> data) {

		if (data.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", "Empty field"));
		}

		try {
			List<Object[]> validatedList = service.validateListExists(data);
			List<Map<String, Object>> response = new ArrayList<>();

			for (Object[] value : validatedList) {
				if (Integer.valueOf(value[1].toString()) == 0) {
					Map<String, Object> notFoundMap = new HashMap<>();
					notFoundMap.put("ImageId not found:", value[0]);
					response.add(notFoundMap);
				}
			}
			if (!response.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			List<Object[]> myList = service.getImagesById(data);

			for (Object[] element : myList) {
				Map<String, Object> map = new HashMap<>();
				BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, element[0].toString()).build();
				URL signedUrl = storage.signUrl(blobInfo, 3, TimeUnit.MINUTES, SignUrlOption.withV4Signature());
				map.put("Image", signedUrl.toString());
				map.put("ImageId", element[0]);
				map.put("Name", element[1]);
				map.put("Description", element[2]);
				map.put("Categories", element[3]);
				response.add(map);
			}
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> map = new HashMap<>();
			map.put("Message", "Inner Error");
			map.put("Error", "Invalid input data field");
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 *GET IMAGES BY CATEGORY/CATEGORIES
	 *IF YOU SELECT 2 CATEGORIES IT RETREIVES IMAGES THAT HAVE THESE CATEGORIES
	 * number= 0 -> ALL number=# -> #
	 */
	@GetMapping("/getImagesByCategory")
	public ResponseEntity<?> getAllImagesByCategory(
			@RequestParam("number") String number,
			@RequestBody List<String> data
			) {

		if (data.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", "Empty field"));
		}

		try {
			
			List<Object[]> validatedList = service.validateCategoryListExists(data);
			List<Map<String, Object>> response = new ArrayList<>();
			
			for (Object[] value : validatedList) {
				if (Integer.valueOf(value[1].toString()) == 0) {
					Map<String, Object> notFoundMap = new HashMap<>();
					notFoundMap.put("Category not found:", value[0]);
					response.add(notFoundMap);
				}
			}
			
			if (!response.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			
						
			List<Object[]> myList = service.getAllImagesByCategory(data, number);

			for (Object[] element : myList) {

				Map<String, Object> map = new HashMap<>();
				BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, element[0].toString()).build();
				URL signedUrl = storage.signUrl(blobInfo, 3, TimeUnit.MINUTES, SignUrlOption.withV4Signature());

				map.put("Image", signedUrl.toString());
				map.put("ImageId", element[0]);
				map.put("Name", element[1]);
				map.put("Description", element[2]);
				map.put("Categories", element[3]);
				response.add(map);
			}

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> map = new HashMap<>();
			map.put("mensaje", "Error interno");
			map.put("error", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 *GET IMAGES BY CATEGORY/CATEGORIES
	 *IF YOU SELECT FANTASY AND BUILDING AS CATEGORIES ONLY IMAGES WITH THAT MATCH WILL BE BROUGHT
	 * number= 0 -> ALL number=# -> #
	 */	
	@GetMapping("/getMatchedImagesByCategory")
	public ResponseEntity<?> getMatchedImagesByCategory(
			@RequestParam("number") String number,
			@RequestBody List<String> data
			){

		if (data.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", "Empty field"));
		}

		try {

			List<Object[]> validatedList = service.validateCategoryListExists(data);
			List<Map<String, Object>> response = new ArrayList<>();
			
			for (Object[] value : validatedList) {
				if (Integer.valueOf(value[1].toString()) == 0) {
					Map<String, Object> notFoundMap = new HashMap<>();
					notFoundMap.put("Category not found:", value[0]);
					response.add(notFoundMap);
				}
			}
			
			if (!response.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			
			List<Object[]> myList = service.getMatchedImagesByCategory(data, number);

			for (Object[] element : myList) {

				Map<String, Object> map = new HashMap<>();
				BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, element[0].toString()).build();
				URL signedUrl = storage.signUrl(blobInfo, 3, TimeUnit.MINUTES, SignUrlOption.withV4Signature());

				map.put("Image", signedUrl.toString());
				map.put("ImageId", element[0]);
				map.put("Name", element[1]);
				map.put("Description", element[2]);
				response.add(map);
			}

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> map = new HashMap<>();
			map.put("mensaje", "Error interno");
			map.put("error", e.getMessage());
			map.put("error", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getCategories")
	public ResponseEntity<?> getCategories() {

		HttpStatus statusResponse = HttpStatus.OK;
		List<Map<String, Object>> myResponse = new ArrayList<>();

		try {
			
			String subscriptionType = (String) request.getAttribute("subscriptionType");
			
			List<Object[]> myList = service.getCategories();
			Map<String, Object> response = new HashMap<>();
			response.put("Categories", myList);
			myResponse.add(response);
			return new ResponseEntity<>(myResponse, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("mensaje", "Error interno");
			response.put("error", e.getMessage());
			myResponse.add(response);
			return new ResponseEntity<>(myResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}