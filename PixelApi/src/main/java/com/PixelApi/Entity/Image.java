package com.PixelApi.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "IMAGE")
public class Image {

	@Id
	@Column(name = "IMAGE_ID")
	private String imageId;
	@Column(name = "NAME")
	private String name;
	@Column(name = "DESCRIPTION")
	private String description;

	////////////////////////////////////////////////////////////////

	public Image() {

	}
	public Image(String imageId, String name) {
		this.imageId = imageId;
		this.name = name;
	}
	
	public Image(String imageId, String name, String description) {
		this.imageId = imageId;
		this.name = name;
		this.description = description;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}