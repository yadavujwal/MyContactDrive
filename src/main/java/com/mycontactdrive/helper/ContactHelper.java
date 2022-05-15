package com.mycontactdrive.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import com.mycontactdrive.entities.Contact;
import com.mycontactdrive.entities.User;

public class ContactHelper {
	public static boolean createFolder(int uid) throws Exception {

		String path = new ClassPathResource("static/img").getFile().getAbsolutePath();
		System.out.println(path);
		path = path + File.separator + uid + File.separator + "contact_images";
		System.out.println(path);
		File f = new File(path);

		boolean flag = f.mkdir();
		return flag;

	}

	public static void setDefaultImage(User u, InputStream is, String destPath) throws Exception {

		try {
			byte[] data = new byte[is.available()];
			is.read(data);
			FileOutputStream fos = new FileOutputStream(destPath);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setProfile(String dp, MultipartFile f) throws Exception {
		try (InputStream is = f.getInputStream()) {
			byte[] data = new byte[is.available()];
			is.read(data);
			FileOutputStream fos = new FileOutputStream(dp);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			throw new Exception("Could not save image file: " + e);
		}
	}
}
