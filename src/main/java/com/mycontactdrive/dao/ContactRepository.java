package com.mycontactdrive.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycontactdrive.entities.Contact;
import com.mycontactdrive.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	//pagenation 
	
	//current page 
	//contacts per page
	@Query("from Contact as c where c.user.id = :uid order by c.name")
	public Page<Contact> findContactsByUser(@Param("uid")int uid,Pageable pageable);

	@Modifying
    @Query("DELETE FROM Contact c WHERE c.cid = :id")
    void deleteById(@Param("id") Integer id);
	
	@Query("Select count (c) from Contact c where c.image = :image and c.user.id = :uid")
	public int getCountOfImages(@Param("image") String image, @Param("uid") Integer uid);
	
	public List<Contact> findByNameContainingAndUser(String keyword, User user);
}
