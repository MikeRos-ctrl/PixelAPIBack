package com.PixelApi.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.PixelApi.Entity.CategoryImage;
import com.PixelApi.Entity.ImageCategoryDTO;

@Repository
public interface CategoryImageRepo extends JpaRepository<CategoryImage, Long> {

	@Query("SELECT NEW CategoryImage(categoryId,imageId) FROM CategoryImage where categoryId=:categoryId AND imageId NOT IN (:imageId) ORDER BY RAND(:seed) LIMIT 4")
	List<CategoryImage> getImagesByCategory(@Param("seed") Long seed, @Param("categoryId") int categoryId , @Param("imageId") String imageId);
	
	@Query("SELECT DISTINCT NEW CategoryImage(categoryId,imageId) FROM CategoryImage where imageId NOT IN (:imageId) ORDER BY RAND(:seed) LIMIT 4")
	List<CategoryImage> getImagesByCategory2(@Param("seed") Long seed, @Param("imageId") String imageId);

	@Query(value = """
			SELECT
			    CM.IMAGE_ID AS imageId,
			    (SELECT `NAME` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS imageName,
			    (SELECT `DESCRIPTION` FROM IMAGE WHERE IMAGE_ID = CM.IMAGE_ID) AS imageDescription,
			    GROUP_CONCAT(C.`NAME`) AS categoryNames
			FROM
			    CATEGORY_IMAGE CM
			INNER JOIN
			    CATEGORY C
			ON
			    CM.CATEGORY_ID = C.CATEGORY_ID
			GROUP BY
			    CM.IMAGE_ID
			ORDER BY
			    RAND(:seed)
			LIMIT 1
			""", nativeQuery = true)
	List<Object[]> getRandomImageWithCategories(@Param("seed") Long seed);
}