package com.PixelApi.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.PixelApi.Entity.Client;
import com.PixelApi.Repository.ClientRepo;
import com.PixelApi.Service.ClientService;
import org.springframework.security.core.userdetails.User;

/*
 * First security filter, First of all i revise if an User exists in my db registers, if it exists
 * I upload that user in my spring security context and is ready for the SecurityFilterChain
 */

@Service
public class AuthUser implements UserDetailsService{

	@Autowired
	ClientRepo clientRepo;
	
	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
	
		Client myUser = clientRepo.findById(Long.parseLong(id)).get();
		
		return User.builder()
				.username(myUser.getClientId().toString())
				.password(myUser.getAcctKey())
				.roles(myUser.getRole().toString())
				.accountLocked(false)
				.disabled(false)
				.build();
	}
	
	/*
	 *Spring Security compares the provided password (from the Authorization header) 
	 *with the password in the User object, 
	 *typically using a PasswordEncoder like BCryptPasswordEncoder.  
	 */
	@Bean
	public PasswordEncoder myEncoder() {
		return new BCryptPasswordEncoder();
	}
}