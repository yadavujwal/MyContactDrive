package com.mycontactdrive.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mycontactdrive.dao.ContactRepository;
import com.mycontactdrive.dao.UserRepository;
import com.mycontactdrive.entities.Contact;
import com.mycontactdrive.entities.User;

@RestController
public class SearchController {

	@Autowired
	private UserRepository ur;
	
	@Autowired
	private ContactRepository cr;
	
	//search handler
	@GetMapping("/search/{kw}")
	public ResponseEntity<?> search(@PathVariable("kw") String kw, Principal p){
		System.out.println(kw);
		User user = this.ur.getUserByUserName(p.getName());
		List<Contact> contacts = this.cr.findByNameContainingAndUser(kw, user);
		return ResponseEntity.ok(contacts);
	}
	
}
