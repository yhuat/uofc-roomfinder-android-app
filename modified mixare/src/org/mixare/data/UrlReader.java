package org.mixare.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class UrlReader {

	/**
	 * 
	 * @param stringUrl
	 * @return a string with the content of the page
	 */
	public static String readFromURL(String stringUrl) {
		InputStream is = null;
		BufferedReader rd = null;
		StringBuilder sb = new StringBuilder();
		int cp;

		try {
			is = stringToUri(stringUrl).toURL().openStream();
			rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

			// read whole page
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			return sb.toString();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rd != null)
					rd.close();
			} catch (IOException e) {
			}
			
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static URI stringToUri(String stringUrl) throws IOException, MalformedURLException, URISyntaxException {
		// escape URL
		stringUrl = stringUrl.replace("http://", "");
		String hostname = stringUrl.split("/")[0];
		String path = stringUrl.replace(hostname, "");

		// query params set?
		String query = null;
		if (path.indexOf('?') != -1) {				
			query = path.substring(path.indexOf('?')+1, path.length());
			path = path.split("\\?")[0];
		}

		// cut off port (if set)
		int port = 80;
		if (hostname.indexOf(':') != -1) {
			port = Integer.parseInt(hostname.substring(hostname.indexOf(':')+1, hostname.length()));
			hostname = hostname.substring(0, hostname.indexOf(':'));
		}
		
		//System.out.println("splitted: " + hostname + "    :    " + port + " - " + path + "    ?     " + query);
		//System.out.println(new URI("http", null, hostname, 80, path, query, null).toURL());

		// open stream for URL
		return new URI("http", null, hostname, port, path, query, null);
	}
}
