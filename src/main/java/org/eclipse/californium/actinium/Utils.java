package org.eclipse.californium.actinium;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by ynh on 02/11/15.
 */
public class Utils {

    public static String readFile(File file) {
        try {
            return new Scanner(file).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String readFile(URL resource) {
        try {
            return readFile(new File(resource.toURI()));
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
