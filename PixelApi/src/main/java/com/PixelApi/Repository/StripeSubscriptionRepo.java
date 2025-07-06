package com.PixelApi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PixelApi.Entity.StripeSubscription;

@Repository
public interface StripeSubscriptionRepo extends JpaRepository<StripeSubscription, String>{

}
