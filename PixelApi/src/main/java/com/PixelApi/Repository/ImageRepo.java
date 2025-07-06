package com.PixelApi.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PixelApi.Entity.CategoryImage;
import com.PixelApi.Entity.Image;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ImageRepo extends JpaRepository<Image, String> {

	@Query("SELECT NEW Image(imageId,name) FROM Image ORDER BY RAND(:seed) LIMIT 5")
	List<Image> FillFront(@Param("seed") Long seed);
}