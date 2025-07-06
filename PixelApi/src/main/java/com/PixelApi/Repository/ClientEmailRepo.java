package com.PixelApi.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PixelApi.Entity.ClientEmail;

@Repository
public interface ClientEmailRepo extends JpaRepository<ClientEmail, String> {

	//Long countByClientId(Long client);

	//Token findByClientId(String id);

	@Query("SELECT COUNT(t) FROM ClientEmail t WHERE t.clientId=:clientId AND t.reason=:reason AND t.used=:used")
	Long tokenValidation(
			@Param("clientId") Long clientId, 
			@Param("reason") String reason,
			@Param("used") Boolean activa);

}