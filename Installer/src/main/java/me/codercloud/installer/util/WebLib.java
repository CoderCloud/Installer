package me.codercloud.installer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.BitSet;

public class WebLib {

private static final BitSet noEncoding;
	
	static {
		noEncoding = new BitSet(256);
		for (int i = 97; i <= 122; ++i)
			noEncoding.set(i);
		for (int i = 65; i <= 90; ++i)
			noEncoding.set(i);
		for (int i = 48; i <= 57; ++i)
			noEncoding.set(i);
		noEncoding.set(45);
		noEncoding.set(95);
		noEncoding.set(46);
		noEncoding.set(42);
	}
	
	/**
	 * Connect to a http address with a get request
	 * @param s The url
	 * @return The recieved data
	 * @throws IOException If something went wrong
	 */
	public static byte[] getWebsite(String s) throws IOException {
		return getWebsitePost(s, null);
	}
	
	/**
	 * Connect to a http address with a get request
	 * @param s The url
	 * @param postData The postdata to be sent
	 * @return The recieved data
	 * @throws IOException If something went wrong
	 */
	public static byte[] getWebsitePost(String s, byte[] postData) throws IOException {
		try {
			URL url = new URL(s);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestProperty("User-Agent", "CCore");

			
			if(postData==null) {
				c.setRequestMethod("GET");
			} else {
				c.setRequestMethod("POST");
				c.setDoOutput(true);
				OutputStream os = c.getOutputStream();
				os.write(postData);
				os.flush();
				os.close();
			}
			
			int i = c.getResponseCode();
			
			switch(i) {
			case 200:
				break;
			case 301:
			case 302:
			case 303:
				return getWebsitePost(c.getHeaderField("Location"), postData);
			default:
				throw new IOException("Responsecode: " + i + " " + c.getResponseMessage());
			}
			
			InputStream in = c.getInputStream();
			
			return IOLib.readStreamFully(in);
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * Encodes a text for URLs
	 * @param s The text
	 * @return The encoded text
	 */
	public static String urlEncode(String s) {
		StringBuilder b = new StringBuilder(s.length());
		for(char c : s.toCharArray()) {
			if(noEncoding.get(c))
				b.append(c);
			else {
				b.append('%');
				char c1 = Character.forDigit(c >> 4 & 0xF, 16);
				if(Character.isLetter(c1))
					c1 -= ' ';
				b.append(c1);
				c1 = Character.forDigit(c & 0xF, 16);
				if(Character.isLetter(c1))
					c1 -= ' ';
				b.append(c1);
			}
		}
		return b.toString();
	}
	
}
