package com.mycontactdrive.helper;

//C:\Users\DELL\Documents\workspace-spring-tool-suite-4-4.13.1.RELEASE\MyContactDrive\target\classes\static\img\28
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import com.mycontactdrive.entities.User;

public class UserHelper {
	
	public static boolean createFolder(int uid) throws Exception {
		
		System.out.println("call hua h");
		String path = new ClassPathResource("static/img").getFile().getAbsolutePath();
		System.out.println(path);
		path = path + File.separator+ uid;
		System.out.println(path);
		File f = new File(path);
		
		boolean flag = f.mkdir();
		return flag;
		
	}
	
	public static void setProfile(User u, InputStream is) throws Exception  {
		String homepath = new ClassPathResource("static/img/"+u.getId()).getFile().getAbsolutePath()  + File.separator+ u.getImageUrl();
		try {
			byte[] data = new byte[is.available()];
			is.read(data);
			FileOutputStream fos = new FileOutputStream(homepath);
			fos.write(data);
			fos.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
