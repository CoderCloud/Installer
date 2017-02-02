package me.codercloud.installer.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;

public class IOLib {
	
	/**
	 * Read all the content from a {@link InputStream}
	 * @param is The {@link InputStream}
	 * @return The {@link Byte}s read
	 * @throws IOException As in {@link InputStream#read()}
	 * @see #transferStreamFully(InputStream, OutputStream)
	 */
	public static byte[] readStreamFully(InputStream is) throws IOException {
		int i = is.available();
		ByteArrayOutputStream os = new ByteArrayOutputStream(i>0?i:32);
		transferStreamFully(is, os);
		return os.toByteArray();
	}
	
	/**
	 * Read all the content from a {@link InputStream} and convert it to a {@link String}
	 * @param s The {@link InputStream}
	 * @return The read content
	 * @throws IOException As in {@link InputStream#read()}
	 * @see #readStreamFully(InputStream)
	 */
	public static String readStreamAsString(InputStream s) throws IOException {
		return new String(readStreamFully(s), Charset.defaultCharset());
	}
	
	/**
	 * Transfers all the content from a {@link InputStream} to a {@link OutputStream}
	 * @param in The {@link InputStream}
	 * @param out The {@link OutputStream}
	 * @throws IOException As in {@link InputStream#read()} and {@link OutputStream#write(int)}
	 */
	public static void transferStreamFully(InputStream in, OutputStream out) throws IOException {
		int i;
		byte[] buff = new byte[256];
		while((i = in.read(buff, 0, buff.length)) >= 0) {
			out.write(buff, 0, i);
		}
	}
	
	/**
	 * Close any {@link Closeable} and ignore all {@link Exception}s
	 * @param c The {@link Closeable}
	 */
	public static void close(Object c) {
		if(c == null)
			return;
		try {
			if(c instanceof Closeable)
				((Closeable) c).close();
			else {
				Method m = c.getClass().getMethod("close", new Class<?>[0]);
				if(!Modifier.isStatic(m.getModifiers()))
					m.invoke(c, new Object[0]);
			}
		} catch(Exception e) {}
	}
	
}
