package com.mycontactdrive.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.mycontactdrive.dao.ContactRepository;
import com.mycontactdrive.dao.UserRepository;
import com.mycontactdrive.entities.Contact;
import com.mycontactdrive.entities.User;
import com.mycontactdrive.helper.ContactHelper;
import com.mycontactdrive.helper.Message;
import com.mycontactdrive.helper.UserHelper;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository ur;
	@Autowired
	private ContactRepository cr;
	
	@Autowired
	private BCryptPasswordEncoder bcp;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal p) {
		String userName = p.getName();
		User user = ur.getUserByUserName(userName);
		//System.out.println(user);
		m.addAttribute("user", user);
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m) {
		m.addAttribute("title", "Dashboard");
		return "normal/user_dashboard";
	}

	// add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add-contact form
	@PostMapping(value = { "/process-contact" })
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult res,
			@RequestParam("image1") MultipartFile file, Principal p, Model m, HttpSession hp) {
		if (res.hasErrors()) {
			// System.out.println(res.toString());
			m.addAttribute("ERROR ", res.toString());
			m.addAttribute("contact", contact);
			return "normal/add_contact_form";
		}
		try {
			String name = p.getName();
			User user = this.ur.getUserByUserName(name);
			// processing and uploading file
			if (file.isEmpty()) {
				// default image will be uploaded
				String fileName = "default.png";
				String defaultKaPath = new ClassPathResource("static/img").getFile().getAbsolutePath() + File.separator
						+ "default.png";
				contact.setImage(fileName);
				String destPath = new ClassPathResource("static/img").getFile().getAbsolutePath() + File.separator
						+ user.getId() + File.separator + "contact_images" + File.separator + fileName;
				File f = new File(defaultKaPath);
				InputStream is = new FileInputStream(f);
				ContactHelper.setDefaultImage(user, is, destPath);
			} else {
				// uploaded the uploaded file and update the file name in contact table
				contact.setImage(file.getOriginalFilename());
				String filename = file.getOriginalFilename();
				String destinationPath = new ClassPathResource("static/img").getFile().getAbsolutePath()
						+ File.separator + user.getId() + File.separator + "contact_images" + File.separator + filename;
				ContactHelper.setProfile(destinationPath, file);
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.ur.save(user);
//			System.out.println("Added to database");
			//message success
			hp.setAttribute("message", new Message("Your contact is added successfully !","success" ));
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			//error message
			hp.setAttribute("message", new Message("Something went wrong, try again !","danger" ));
		}
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page = 5[n]
	//current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m, Principal p) {
		m.addAttribute("title", "Contact List");
		//contact ki list ko bhejni hai
		String username = p.getName();
		User user = this.ur.getUserByUserName(username);
		//method 1 using this directly
		//List<Contact> list = user.getContacts();
		
		//method 2
		//Using contact repository
		Pageable pga = PageRequest.of(page, 5);
		Page<Contact> contacts = this.cr.findContactsByUser(user.getId(), pga);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		m.addAttribute("currUser", user);
		m.addAttribute("tp", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model m, Principal p) {
		//System.out.println(cid);
		Optional<Contact> contactOptional = this.cr.findById(cid);
		Contact contact = contactOptional.get();
		String username = p.getName();
		User currUser = this.ur.getUserByUserName(username);
		if(contact.getUser().getId() == currUser.getId()) {
			m.addAttribute("contact",contact);
			m.addAttribute("currUser",currUser);
			m.addAttribute("title", contact.getName());
		}
		else {
			m.addAttribute("title", "Permission Denied");
		}
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model m, Principal p, HttpSession session) throws IOException {
		Optional<Contact> contactOp = this.cr.findById(cid);
		Contact contact = contactOp.get();
		String username = p.getName();
		User currUser = this.ur.getUserByUserName(username);
		if(currUser.getId() == contact.getUser().getId()) {
			contact.setUser(null);
			if(this.cr.getCountOfImages(contact.getImage(), currUser.getId()) <= 1) {
				String filePath = new ClassPathResource("static/img/"+currUser.getId()).getFile().getAbsolutePath()  + File.separator+ "contact_images"+ File.separator+ contact.getImage(); 
				Files.delete(Paths.get(filePath));
			}
			this.cr.deleteById(contact.getCid());
			session.setAttribute("message", new Message("Contact deleted successfully !", "success"));
		}
		return "redirect:/user/show-contacts/0";
	}
	
	//update contact
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");
		Optional<Contact> contactOp = this.cr.findById(cid);
		Contact contact = contactOp.get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value= "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("image1") MultipartFile file, Model m, HttpSession session, Principal p) {
		try {
			//old contact details
			Contact oldcontact = this.cr.findById(contact.getCid()).get();
			User cu = this.ur.getUserByUserName(p.getName());
			//image
			if(!file.isEmpty()) {
				//old vale ko delete kiya
				if(this.cr.getCountOfImages(oldcontact.getImage(), cu.getId()) <= 1) {
					String filePath = new ClassPathResource("static/img/"+cu.getId()).getFile().getAbsolutePath()  + File.separator+ "contact_images"+ File.separator+ oldcontact.getImage(); 
					Files.delete(Paths.get(filePath));
				}
				//naye vale ko set kiya
				String destinationPath = new ClassPathResource("static/img").getFile().getAbsolutePath()+ File.separator + cu.getId() + File.separator + "contact_images" + File.separator + file.getOriginalFilename();
				ContactHelper.setProfile(destinationPath, file);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldcontact.getImage());
			}
			contact.setUser(cu);
			this.cr.save(contact);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		session.setAttribute("message", new Message("Contact updated successfully !", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m, Principal p) {
		User cu = this.ur.getUserByUserName(p.getName());
		m.addAttribute("title", cu.getName());
		m.addAttribute("user", cu);
		return "normal/profile";
	}
	
	//update-profile handler
	@PostMapping("/update-user")
	public String updateProfile(Principal p, Model m) {
		m.addAttribute("title", "Update Profile");
		User user  = this.ur.getUserByUserName(p.getName());
		m.addAttribute("user", user);
		//System.out.println(user.getId());
		return "normal/update_profile";
	}
	
	//process update profile handler
	@PostMapping("process-user-update")
	public String updateUserHandler(Principal p, @ModelAttribute User updatedData,  @RequestParam("image1") MultipartFile file, Model m, HttpSession session) {
		try {
			//old contact details
			User olduser = this.ur.getUserByUserName(p.getName());
			
			//image
			if(!file.isEmpty()) {
				
				String destinationPath = new ClassPathResource("static/img").getFile().getAbsolutePath()+ File.separator + olduser.getId() + File.separator + file.getOriginalFilename();
				ContactHelper.setProfile(destinationPath, file);
				updatedData.setImageUrl(file.getOriginalFilename());
			}
			else {
				updatedData.setImageUrl(olduser.getImageUrl());
			}
			this.ur.save(updatedData);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		session.setAttribute("message", new Message("User updated successfully !", "success"));
		return "redirect:/user/profile";
	}
	
	//settings page
	@GetMapping("/settings")
	public String settingsPage(Model m){
		m.addAttribute("title", "Settings");
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("opass") String opass,
			@RequestParam("npass") String npass,
			@RequestParam("cpass") String cpass,
			Principal p, HttpSession session, Model m) {
		
		if( !npass.equals(cpass) ){
			session.setAttribute("message", new Message("New Password and Confirm Password are not matching !", "danger"));
			return "normal/settings";
		}
		
		User user = this.ur.getUserByUserName(p.getName());
		String currPassword = user.getPassword();
		
		if(this.bcp.matches(opass, currPassword)) {
			//change the password
			user.setPassword(this.bcp.encode(npass));
			this.ur.save(user);
			session.setAttribute("message", new Message("Password has been successfully changed !", "success"));
		}
		else{
			session.setAttribute("message", new Message("Old Password is incorrect !", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}
