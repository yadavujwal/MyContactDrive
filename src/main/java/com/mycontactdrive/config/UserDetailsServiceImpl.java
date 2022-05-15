package com.mycontactdrive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mycontactdrive.dao.UserRepository;
import com.mycontactdrive.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository ur;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//fetching user from database
		User user = ur.getUserByUserName(username);
		if(user == null) {
			throw new UsernameNotFoundException("Could not found User");
		}
		
		CustomUserDetails cud = new CustomUserDetails(user);
		return cud;
	}

}
