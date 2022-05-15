package com.mycontactdrive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotController {
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	//send otp handler
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email) {
		System.out.println(email);
		//generating otp of 4 digit
		int otp = (int)Math.floor(Math.random()*(999999-100000+1)+100000);
		System.out.println(otp);
		return "verify_otp";
	}
}
