package com.PixelApi.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CATEGORY_IMAGE")
public class CategoryImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "CATEGORY_ID")
	private String categoryId;

	@Column(name = "IMAGE_ID")
	private String imageId;

	public CategoryImage() {
	}

	public CategoryImage(String categoryId, String imageId) {
		this.categoryId = categoryId;
		this.imageId = imageId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
}