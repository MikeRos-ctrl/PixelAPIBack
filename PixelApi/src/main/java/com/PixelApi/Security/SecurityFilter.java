package com.PixelApi.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.PixelApi.Filter.JwtFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityFilter {

	private final JwtFilter jwtFilter;

	@Autowired
	public SecurityFilter(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/frontController/confirmAccount/{token}").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/login/{email}/{accountKey}").permitAll()
						.requestMatchers(HttpMethod.PUT, "/frontController/updateAccount").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/forgotPwd/{email}").permitAll()
						.requestMatchers(HttpMethod.POST, "/frontController/createAccount").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/validateAccount/{email}").permitAll()
						.requestMatchers(HttpMethod.POST, "/frontController/stripeSubscription").permitAll()
						.requestMatchers(HttpMethod.POST, "/frontController/freeSubscription").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/getRandomImageWithCategories").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/stripeCredentials").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/fillFrontHeader").permitAll()
						.requestMatchers(HttpMethod.GET, "/frontController/listByCategory/{category}/{imageId}").permitAll()
						.anyRequest().authenticated())
				.httpBasic(withDefaults())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}