package com.PixelApi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PixelApi.Entity.Client;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {

	Client findByEmail(String email);

	Long countByEmail(String email);
}