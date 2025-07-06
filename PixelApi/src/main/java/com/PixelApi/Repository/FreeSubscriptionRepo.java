package com.PixelApi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PixelApi.Entity.FreeSubscription;
import com.PixelApi.Entity.StripeSubscription;

@Repository
public interface FreeSubscriptionRepo extends JpaRepository<FreeSubscription, String>{

	FreeSubscription findByClientId(Long id);

}