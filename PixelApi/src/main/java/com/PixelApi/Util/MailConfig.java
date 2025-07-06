package com.PixelApi.Util;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.hostinger.com");
		mailSender.setPort(587);
		mailSender.setUsername("pixelteam@thepixelapi.com");
		mailSender.setPassword("MikeAndChaeyoung<3");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true"); 
		props.put("mail.smtp.starttls.required", "true"); 
		props.put("mail.debug", "true");

		return mailSender;
	}
}