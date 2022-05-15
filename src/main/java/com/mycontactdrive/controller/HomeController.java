package com.mycontactdrive.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mycontactdrive.dao.UserRepository;
import com.mycontactdrive.entities.User;
import com.mycontactdrive.helper.ContactHelper;
import com.mycontactdrive.helper.Message;
import com.mycontactdrive.helper.UserHelper;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - MyContactDrive");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - MyContactDrive");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Signup - MyContactDrive");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handling for registering user
	@RequestMapping(value = "/process_form", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult res,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m, HttpSession hs) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			if(res.hasErrors()) {
				m.addAttribute("ERROR ",res.toString() );
				m.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User result = this.userRepository.save(user);
			
			boolean flag = UserHelper.createFolder(user.getId());
			boolean flag1 = ContactHelper.createFolder(user.getId());
			
			
			String path = new ClassPathResource("static/img").getFile().getAbsolutePath() + "/default.png";
			File f = new File(path);
			InputStream is = new FileInputStream(f);
			UserHelper.setProfile(user, is);
			
			m.addAttribute("user", new User());
			hs.setAttribute("message", new Message("Successfully Registered", "alert-success"));
			return "signup";
		}
		catch(Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			hs.setAttribute("message", new Message("Something went wrong !!"+ e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}

	//handler for custom login
	@RequestMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "Signin - MyContactDrive");
		return "login";
	}
}
