package com.PixelApi.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.PixelApi.Entity.CategoryImage;
import com.PixelApi.Entity.Image;
import com.PixelApi.Entity.ImageCategoryDTO;
import com.PixelApi.Repository.CategoryImageRepo;
import com.PixelApi.Repository.ImageRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class ImageService {

	@Autowired
	ImageRepo repo;

	@Autowired
	CategoryImageRepo repo2;

	@Autowired
	private EntityManager entityManager;

	public String findNameById(String id) {
		return (repo.findById(id).get()).getName();
	}

	public List<Image> FillFront() {
		return repo.FillFront(System.currentTimeMillis());
	}

	public List<CategoryImage> getImagesByCategory(int category, String imageId) {
		return repo2.getImagesByCategory(System.currentTimeMillis(), category, imageId);
	}

	public List<CategoryImage> getImagesByCategory2(String imageId) {
		return repo2.getImagesByCategory2(System.currentTimeMillis(), imageId);
	}

	public ImageCategoryDTO getRandomImageWithCategories() {
		List<Object[]> resultList = repo2.getRandomImageWithCategories(System.currentTimeMillis());

		Object[] result = resultList.get(0);

		return new ImageCategoryDTO((String) result[0], (String) result[1], (String) result[2], (String) result[3]);
	}

	public List<Object[]> getOrderedImages(String number) {

		String sql = "";

		if (number.equals("0")) {
			sql = """
					SELECT
					    CM.IMAGE_ID,
					    (SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_NAME,
					    (SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_DESCRIPTION,
					    GROUP_CONCAT(C.`NAME`) AS CATEGORY_NAME
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C ON CM.CATEGORY_ID = C.CATEGORY_ID
					GROUP BY CM.IMAGE_ID""";
		} else {
			sql = """
					SELECT
					    CM.IMAGE_ID,
					    (SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_NAME,
					    (SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_DESCRIPTION,
					    GROUP_CONCAT(C.`NAME`) AS CATEGORY_NAME
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C ON CM.CATEGORY_ID = C.CATEGORY_ID
					GROUP BY CM.IMAGE_ID
					LIMIT :number""";
		}

		Query query = entityManager.createNativeQuery(sql);
		if (!number.equals("0"))
			query.setParameter("number", Integer.parseInt(number));
		List<Object[]> results = query.getResultList();

		return results;
	}

	public List<Object[]> getRandomImages(String number) {
		String sql = "";

		if (number.equals("0")) {
			sql = """
					SELECT
					    CM.IMAGE_ID,
					    (SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_NAME,
					    (SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_DESCRIPTION,
					    GROUP_CONCAT(C.`NAME`) AS CATEGORY_NAME
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C ON CM.CATEGORY_ID = C.CATEGORY_ID
					GROUP BY CM.IMAGE_ID
					ORDER BY RAND(:seed)""";
		} else {
			sql = """
					SELECT
					    CM.IMAGE_ID,
					    (SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_NAME,
					    (SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS IMAGE_DESCRIPTION,
					    GROUP_CONCAT(C.`NAME`) AS CATEGORY_NAME
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C ON CM.CATEGORY_ID = C.CATEGORY_ID
					GROUP BY CM.IMAGE_ID
					ORDER BY RAND(:seed)
					LIMIT :number""";
		}

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("seed", System.currentTimeMillis());
		if (!number.equals("0"))
			query.setParameter("number", Integer.parseInt(number));

		List<Object[]> results = query.getResultList();

		return results;
	}

	public List<Object[]> validateListExists(List<String> data) {
		String sql = "";
		String sql2 = "";
		int counter = 0;
		
		for(String currentValue : data) {

			if(counter == 0) {
				sql += "SELECT '"+ currentValue +"' AS IMAGE_ID ";		
				counter++;
			}
			else {
				sql += "UNION ALL ";	
				sql += "SELECT '"+ currentValue +"'";	
			}
		}

		sql2 = """
				SELECT v.IMAGE_ID,
				       CASE WHEN img.IMAGE_ID IS NOT NULL THEN 1 ELSE 0 END AS "EXISTS"
				FROM (
				"""
				+
				sql
				+
				"""
				) AS v
				LEFT JOIN image img ON v.IMAGE_ID = img.IMAGE_ID;
				""";

		Query query = entityManager.createNativeQuery(sql2);
		List<Object[]> results = query.getResultList();

		return results;
	}
	
	public List<Object[]> validateCategoryListExists(List<String> data) {
		String sql = "";
		String sql2 = "";
		int counter = 0;
		
		for(String currentValue : data) {

			if(counter == 0) {
				sql += "SELECT '"+ currentValue +"' AS NAME ";		
				counter++;
			}
			else {
				sql += "UNION ALL ";	
				sql += "SELECT '"+ currentValue +"'";	
			}
		}

		sql2 = """
				SELECT v.NAME, 
				   CASE WHEN c.NAME IS NOT NULL THEN TRUE ELSE FALSE END AS "EXISTS"
				FROM (
				"""
				+
				sql
				+
				"""
				) AS v
				LEFT JOIN CATEGORY c ON v.NAME = c.NAME;
				""";

		Query query = entityManager.createNativeQuery(sql2);
		List<Object[]> results = query.getResultList();

		return results;
	}

	public List<Object[]> getImagesById(List<String> data) {

		String sql = """
				SELECT
				CM.IMAGE_ID,
				(SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) as IMAGE_NAME,
				(SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) as IMAGE_DESCRIPTION,
				GROUP_CONCAT(`NAME`) AS CATEGORIES
				FROM CATEGORY_IMAGE CM
				INNER JOIN CATEGORY C
				on CM.CATEGORY_ID = C.CATEGORY_ID
				WHERE CM.IMAGE_ID IN (:elements)
				GROUP BY IMAGE_ID
				""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("elements", data);

		List<Object[]> results = query.getResultList();
		return results;
	}

	/*
	 * IF LIMIT EQUALS 0 MEANS THAT HAS NO LIMIT AT ALL, BRING TI ALL
	 */
	public List<Object[]> getAllImagesByCategory(List<String> data, String number) {

		String sql = "";
		Query query;

		if (number.equals("0")) {

			sql = """
					SELECT
					CM.IMAGE_ID,
					(SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) as IMAGE_NAME,
					(SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) as IMAGE_DESCRIPTION,
					GROUP_CONCAT(`NAME`) AS CATEGORIES
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C
					on CM.CATEGORY_ID = C.CATEGORY_ID
					WHERE C.NAME in (:data)
					GROUP BY IMAGE_ID;
									""";
			query = entityManager.createNativeQuery(sql);
			query.setParameter("data", data);
		} else {

			sql = """
					SELECT
					CM.IMAGE_ID,
					(SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) as IMAGE_NAME,
					(SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) as IMAGE_DESCRIPTION,
					GROUP_CONCAT(`NAME`) AS CATEGORIES
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C
					on CM.CATEGORY_ID = C.CATEGORY_ID
					WHERE C.NAME in (:data)
					GROUP BY IMAGE_ID
					LIMIT :number""";

			query = entityManager.createNativeQuery(sql);
			query.setParameter("data", data);
			query.setParameter("number", Integer.parseInt(number));
		}

		List<Object[]> results = query.getResultList();
		return results;
	}

	public List<Object[]> getMatchedImagesByCategory(List<String> data, String number) {

		String sql = "";
		Query query;

		if (number.equals("0")) {

			sql = """
					SELECT CM.IMAGE_ID,
					       I.NAME AS IMAGE_NAME,
					       I.DESCRIPTION AS IMAGE_DESCRIPTION
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C ON CM.CATEGORY_ID = C.CATEGORY_ID
					INNER JOIN IMAGE I ON CM.IMAGE_ID = I.IMAGE_ID
					WHERE C.NAME IN (:data)
					GROUP BY CM.IMAGE_ID
					HAVING COUNT(DISTINCT C.NAME) = :size
														""";
			query = entityManager.createNativeQuery(sql);
			query.setParameter("data", data);
			query.setParameter("size", data.size());

		} else {

			sql = """
					SELECT CM.IMAGE_ID,
					       I.NAME AS IMAGE_NAME,
					       I.DESCRIPTION AS IMAGE_DESCRIPTION
					FROM CATEGORY_IMAGE CM
					INNER JOIN CATEGORY C ON CM.CATEGORY_ID = C.CATEGORY_ID
					INNER JOIN IMAGE I ON CM.IMAGE_ID = I.IMAGE_ID
					WHERE C.NAME IN (:data)
					GROUP BY CM.IMAGE_ID
					HAVING COUNT(DISTINCT C.NAME) = :size
					LIMIT :number
										""";

			query = entityManager.createNativeQuery(sql);
			query.setParameter("data", data);
			query.setParameter("size", data.size());
			query.setParameter("number", Integer.parseInt(number));
		}

		List<Object[]> results = query.getResultList();
		return results;
	}

	public List<Object[]> getCategories() {

		String sql = "";
		Query query;

		sql = """
					SELECT NAME FROM CATEGORY
				""";

		query = entityManager.createNativeQuery(sql);
		List<Object[]> results = query.getResultList();
		return results;
	}
}