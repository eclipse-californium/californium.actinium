package org.eclipse.californium.actinium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by ynh on 02/11/15.
 */
public class Utils {

	public static String readFile(File file) {
		try {
			Scanner s = new Scanner(file);
			String contents = s.useDelimiter("\\Z").next();
			s.close();
			return contents;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static String readFile(InputStream file) {
		Scanner s = new Scanner(file);
		String contents = s.useDelimiter("\\Z").next();
		s.close();
		return contents;
	}

	public static String readFile(URL resource) {
		try {
			return readFile(new File(resource.toURI()));
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public static String readFile(String path) {
		return readFile(new File(path));
	}
}
