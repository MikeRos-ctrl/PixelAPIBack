package com.PixelApi.Entity;

public class ImageCategoryDTO {

	private String imageId;
	private String name;
	private String description;
	private String categoryNames;
	private String link;
	
	public ImageCategoryDTO(String imageId, String name, String description, String categoryNames) {
		this.imageId = imageId;
		this.name = name;
		this.description = description;
		this.categoryNames = categoryNames;
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
	public String getCategoryNames() {
		return categoryNames;
	}
	public void setCategoryNames(String categoryNames) {
		this.categoryNames = categoryNames;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
}