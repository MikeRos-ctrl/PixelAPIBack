package com.PixelApi.Filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.PixelApi.Security.Jwt;

//import com.PixelApi.Security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private final Jwt jwt;
	private final UserDetailsService userDetailsService;

	@Autowired
	public JwtFilter(Jwt jwtUtil, UserDetailsService userDetailsService) {
		this.jwt = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		/*
		 *  Validar que sea un Header Authorization valido
		 */
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer")) {
			filterChain.doFilter(request, response);
			return;
		}
		

		/*
		 *  Validar que el JWT sea valido
		 */
		String jwt = authHeader.split(" ")[1].trim();

		if (!this.jwt.isValid(jwt)) {
			filterChain.doFilter(request, response);
			return;
		}

		
		/*
		 *  Cargar el usuario usando mi loadUserByUsername en AuthUser
		 */
		String username = this.jwt.getUsername(jwt);
	
		request.setAttribute("subscriptionType", this.jwt.getSubscriptionType(jwt));
	
		User user = (User) this.userDetailsService.loadUserByUsername(username);

		
		/*
		 *  Cargar al usuario en el contexto de seguridad
		 */
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				user.getUsername(), user.getPassword(), user.getAuthorities()
				);

		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		filterChain.doFilter(request, response);
	}
}