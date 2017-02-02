package me.codercloud.installer.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginUtil {
	
	@SuppressWarnings("unchecked")
	public static boolean unloadPlugin(Plugin plugin) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		if (pluginManager == null)
			return false;

		String name = plugin.getName();

		List<Plugin> plugins = null;
		Map<String, Plugin> lookupNames = null;
		SimpleCommandMap commandMap = null;
		Map<String, Command> knownCommands = null;

		try {
			Field pluginsField = Bukkit.getPluginManager().getClass()
					.getDeclaredField("plugins");
			pluginsField.setAccessible(true);
			plugins = (List<Plugin>) pluginsField.get(pluginManager);

			Field lookupNamesField = Bukkit.getPluginManager().getClass()
					.getDeclaredField("lookupNames");
			lookupNamesField.setAccessible(true);
			lookupNames = (Map<String, Plugin>) lookupNamesField
					.get(pluginManager);

			Field commandMapField = Bukkit.getPluginManager().getClass()
					.getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

			Field knownCommandsField = SimpleCommandMap.class
					.getDeclaredField("knownCommands");
			knownCommandsField.setAccessible(true);
			knownCommands = (Map<String, Command>) knownCommandsField
					.get(commandMap);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		pluginManager.disablePlugin(plugin);

		if (plugins != null && plugins.contains(plugin))
			plugins.remove(plugin);

		if (lookupNames != null && lookupNames.containsKey(name))
			lookupNames.remove(name);
		
		if (commandMap != null) {
			HashSet<String> rem = new HashSet<String>();
			for (Entry<String, Command> e : knownCommands.entrySet()) {
				if (e.getValue() instanceof PluginIdentifiableCommand) {
					if(((PluginIdentifiableCommand) e.getValue()).getPlugin() == plugin)
						rem.add(e.getKey());
				}
			}

			for(String s : rem) {
				Command c = knownCommands.remove(s);
				
				if(c != null && c instanceof PluginIdentifiableCommand && ((PluginIdentifiableCommand) c).getPlugin() == plugin)
					c.unregister(commandMap);
				else if(c != null)
					knownCommands.put(s, c);
			}
		}
		
		ClassLoader c = plugin.getClass().getClassLoader();
		if (c instanceof URLClassLoader) {
			try {
				Method m = URLClassLoader.class.getMethod("close");
				m.setAccessible(true);
				m.invoke(c);
			} catch (Throwable ex) {}
		}
		
		me.codercloud.installer.manager.PluginManager.updateOptions();
		System.gc();
		
		return true;
	}
	
	public static boolean bufferPluginClasses(Plugin p) {
		ClassLoader loader = p.getClass().getClassLoader();
		
		ZipFile zip = null;
		try {
			zip = new ZipFile(getPluginFile(p));
		
		Enumeration<? extends ZipEntry> entries = zip.entries();
	
			while(entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();
				String name = e.getName();
				if(name.lastIndexOf(".") != -1 && name.substring(name.lastIndexOf(".")+1, name.length()).equals("class"))
					try {
						Class.forName(name.substring(0, name.lastIndexOf(".")).replaceAll("/", "."), true, loader);
					} catch (Exception e1) {}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(zip != null)
				try {
					zip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return false;
	}
	
	public static byte[] getDescriptionBytes(File f) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(f);
			ZipEntry e = zip.getEntry("plugin.yml");
			if(e != null)
				return BaseUtil.readFully(zip.getInputStream(e));
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if(zip != null)
					zip.close();
			} catch (Exception e) {}
		}
	}
	
	public static Configuration getDescriptionYml(File f) {
		byte[] desc = getDescriptionBytes(f);
		if(desc == null)
			return null;
		try {
			YamlConfiguration c = new YamlConfiguration();
			c.loadFromString(new String(desc));
			return c;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static PluginDescriptionFile getDescriptionFile(File f) {
		byte[] desc = getDescriptionBytes(f);
		if(desc == null)
			return null;
		try {
			return new PluginDescriptionFile(new ByteArrayInputStream(desc));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static File getPluginFile(Plugin plugin) {
		try {
			Field f = JavaPlugin.class.getDeclaredField("file");
			f.setAccessible(true);
			return (File) f.get(plugin);
		} catch (Exception e) {}
		return null;
	}
	
	public static File findFileForPlugin(File folder, String name) {
		File file = null;
		
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(name);
		
		file = getPluginFile(plugin);
		
		if(file != null)
			return file;
		
		ArrayList<File> files = new ArrayList<File>();
		
		for(File f : folder.listFiles()) {
			if(!f.exists() || !f.isFile())
				continue;
			String fname = f.getName();
			int ind = fname.lastIndexOf(".");
			if(ind != -1 && fname.substring(ind+1, fname.length()).equalsIgnoreCase("jar"))
				files.add(f);
		}
		
		for(File f : files) {
			YamlConfiguration yml = getPluginDescription(f);
			if(yml != null && name.equals(yml.getString("name")))
				return f;
		}
		
		File f = new File(folder, "/" + name + ".jar");
		
		if(!f.exists())
			return f;
		
		int t = 0;
		while(++t<10) {
			f = new File(folder, "/" + name + "_" + t + ".jar");
			if(!f.exists())
				return f;
		}
		
		return null;
	}
	
	public static YamlConfiguration getPluginDescription(File f) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(f);
			ZipEntry e = zip.getEntry("plugin.yml");
			
			if (e == null)
				throw new InvalidPluginException();
			
			
			YamlConfiguration pluginyml = new YamlConfiguration();
			try {
				pluginyml.loadFromString(new String(BaseUtil.readFully(zip.getInputStream(e))));
			} catch (InvalidConfigurationException ex) {
				throw new InvalidPluginException();
			}

			return pluginyml;
		} catch (Exception e) {} finally {
			try {
				zip.close();
			} catch (Exception e) {}
		}
		return null;
	}
	
	public static void convertToDelPlugin(File f) {
		for(Plugin p : Bukkit.getServer().getPluginManager().getPlugins()) {
			if(f.equals(getPluginFile(p)))
				unloadPlugin(p);
		}
		
		PluginDescriptionFile desc = getDescriptionFile(f);
		
		ZipFile zip = null;
		ZipOutputStream zos = null;
		
		try {
			HashMap<String, byte[]> zipdata = new HashMap<String, byte[]>();
			
			
			zip = new ZipFile(f);
			
			Enumeration<? extends ZipEntry> entries = zip.entries();

			while(entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();
				InputStream in = zip.getInputStream(e);
				
				zipdata.put(e.getName(), BaseUtil.readFully(in));
			}
			
			zipdata.put("def_plugin.yml", zipdata.get("plugin.yml"));
			zipdata.put("plugin.yml", createDelYml(desc));
			
			zos = new ZipOutputStream(new FileOutputStream(f));
			
			for(Entry<String, byte[]> e : zipdata.entrySet()) {
				zos.putNextEntry(new ZipEntry(e.getKey()));
				zos.write(e.getValue(), 0, e.getValue().length);
			}
			
		} catch (Exception e) {} finally {
			try {
				zip.close();
			} catch (Exception e) {}
			try {
				zos.close();
			} catch (Exception e) {}
		}
		
	}
	
	public static byte[] createDelYml(PluginDescriptionFile desc) {
		YamlConfiguration c = new YamlConfiguration();
		
		List<String> l = new ArrayList<String>();
		l.add(desc.getName());
		
		c.set("name", desc.getName());
		c.set("version", desc.getVersion());
		c.set("authors", desc.getAuthors());
		c.set("main", "none");
		c.set("depend", l);
		
		return c.saveToString().getBytes();
	}
	
}
