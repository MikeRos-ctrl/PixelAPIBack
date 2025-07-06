package com.PixelApi.Entity;

public class StripeSubscriptionDto {

	String stripeSubscriptionId;
	String email;
	String planTypeId;

	public String getStripeSubscriptionId() {
		return stripeSubscriptionId;
	}
	public void setStripeSubscriptionId(String stripeSubscriptionId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPlanTypeId() {
		return planTypeId;
	}
	public void setPlanTypeId(String planTypeId) {
		this.planTypeId = planTypeId;
	}
}