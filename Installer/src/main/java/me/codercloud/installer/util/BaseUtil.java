package me.codercloud.installer.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;



public class BaseUtil {
	
	/* ### BUKKIT ### */
	
	public static final void handleDisable() {
		interruptTasks();
	}
	
	public static final Inventory createInventory(int rows, String title) {
		title = title == null ? "" : title.length()>32 ? title.substring(0, 29) + "..." : title;
		return Bukkit.createInventory(null, 9*rows, title);
	}
	
	public static ItemStack setName(ItemStack i, String name) {
		if(i == null)
			return i;
		ItemMeta m = i.getItemMeta();
		if(m == null)
			return i;
		
		m.setDisplayName(name);
		
		i.setItemMeta(m);
		return i;
	}
	
	public static ItemStack setLore(ItemStack i, String ... lore) {
		if(i == null)
			return i;
		ItemMeta m = i.getItemMeta();
		if(m == null)
			return i;
		
		ArrayList<String> l = new ArrayList<String>();
		for(String s : lore)
			l.add(s);
		
		m.setLore(l);
		
		i.setItemMeta(m);
		return i;
	}
	
	public static ItemStack setNameAndLore(ItemStack i, String name, String ... lore) {
		return setLore(setName(i, name), lore);
	}
	
	public static void openInventorySilent(Plugin plugin, final HumanEntity h, final Inventory i, final Runnable ... finishListener) {
		InventoryView view = h.getOpenInventory();
		if(h == null || i == null || (view != null && i.equals(view.getTopInventory()))) {
			if (finishListener != null)
				for (Runnable r : finishListener) {
					if (r != null)
						try {
							r.run();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			return;
		}
		
		syncOperation(new Runnable() {
			public void run() {
				try {
					try {
						InventoryView v = h.getOpenInventory();
						
						if(i.equals(v.getTopInventory()))
							return;
						
						Method getHandle = getBukkitMethod("entity.CraftHumanEntity", "getHandle");
						Field activeContainer = getServerField("EntityHuman", "activeContainer");
						Field defaultContainer = getServerField("EntityHuman", "defaultContainer");
						Method changeStates = getServerMethod("Container","transferTo", getServerClass("Container"), getBukkitClass("entity.CraftHumanEntity"));

						Object handle;
						try {
							handle = getHandle.invoke(h);
							Object old = activeContainer.get(handle);
							Object def = defaultContainer.get(handle);
							if (old != def)
								activeContainer.set(handle, def);
							changeStates.invoke(old, def, h);
							Bukkit.getPluginManager().callEvent(
									new InventoryCloseEvent(v));
						} catch (Exception e) {
							e.printStackTrace();
							h.closeInventory();
						}
						
						h.openInventory(i);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				} finally {
					if (finishListener != null)
						for (Runnable r : finishListener) {
							if (r != null)
								try {
									r.run();
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
				}
			}
		});
	}
	
	/* ### ARRAYS ### */
	
	public static <T> T getClipped(T[] array, int index, T fill) {
		if(array == null || array.length == 0)
			return fill;
		if(index<array.length)
			return array[index<0?0:index];
		return array[array.length-1];
		
	}
	
	public static <T> ArrayList<T> toList(T ... objects) {
		ArrayList<T> list = new ArrayList<T>(objects.length);
		for(T obj : objects)
			list.add(obj);
		return list;
	}
	
	/* ### STRINGS ### */
	
	public static <T extends Object> String connect(String a, T ... strings) {
		StringBuilder b = new StringBuilder();
		if(strings.length==0)
			return "";
		int i = 0;
		b.append(strings[i++]);
		while(i<strings.length) {
			b.append(a);
			b.append((Object) strings[i++]);
		}
		return b.toString();
	}
	
	public static String connect(String a, Collection<?> strings) {
		StringBuilder b = new StringBuilder();
		if(a.length()==0)
			return "";
		Iterator<?> iter = strings.iterator();
		if(iter.hasNext())
			b.append(iter.next());
		while(iter.hasNext()) {
			b.append(a);
			b.append((Object) iter.next());
		}
		return b.toString();
	}
	
	public static String colorize(String s) {
		StringBuilder b = new StringBuilder();
		int loc = -1;
		int end = 0;
		while((loc = s.indexOf('&', ++loc)) != -1) {
			b.append(s, end, loc);
			char next;
			if(++loc < s.length())
				next = s.charAt(loc);
			else
				next = '&';
			if(next == '&') {
				b.append('&');
				end = loc+1;
				continue;
			}
			end = loc;
			b.append(ChatColor.COLOR_CHAR);
		}
		if(end < s.length())
			b.append(s, end, s.length());
		return b.toString();
	}
	
	public static String decolorize(String s) {
		return s.replaceAll("&", "&&").replace(ChatColor.COLOR_CHAR, '&');
	}
	
	private static String messagePrefix = null;
	
	public static String prefix(String message) {
		StringBuilder b = new StringBuilder();
		String prefix = prefix();
		if(prefix.length() == 0)
			return message;
		if(message.length()>=2)
			if(message.charAt(0) == ChatColor.COLOR_CHAR)
				return b.append(message).insert(2, ' ').insert(2, prefix).toString();
		return b.append(prefix).append(' ').append(message).toString();
	}
	
	public static String prefix() {
		String prefix = messagePrefix;
		if(prefix == null) {
			Plugin p = getPlugin();
			prefix = p.getDescription().getPrefix();
			if(prefix == null)
				prefix = "<" + p.getName() + ">";
			messagePrefix = prefix;
		}
		return prefix;
	}
	
	public static void setPrefix(String prefix) {
		messagePrefix = prefix;
	}
	
	public static String parse(String s, Object ... values) {
		StringBuilder b = new StringBuilder();
		int lastEnd = 0;
		int index = 0;
		
		while((index = s.indexOf('<', (lastEnd = index))) != -1) {
			if(index>0 && s.charAt(index-1) == '\\') {
				index++;
				continue;
			}
			b.append(s, lastEnd, index);
						
			int start = index++;
			int end;
			for(end = start;end<s.length(); end++) {
				if(s.charAt(end) == '\\') {
					end++;
					continue;
				}
				if(s.charAt(end) == '>')
					break;
			}

			end = end<s.length()?end:s.length();
			start++;
			index = end;
			index++;
			String str = s.substring(start, end);
			String replacement = null;

			try {
				int location = 0;
				int i = 0;
				for(char c; i<str.length() && (c = str.charAt(i))>='0' && c<='9'; i++)
					location = location*10+c-'0';
				location--;
				
				if(i == str.length()) {
					String locationValue;
					if(location<values.length)
						locationValue = String.valueOf(values[location]);
					else
						locationValue = String.valueOf((Object) null);

					replacement = locationValue;
				} else if(str.charAt(i) == '?') {
					
					Object locationValue = null;
					if(location<values.length)
						locationValue = String.valueOf(values[location]);
					if(locationValue == null)
						locationValue = String.valueOf((Object) null);
					
					String def = String.valueOf(locationValue);
					boolean found = false;
					while(i<str.length()) {
						int i0 = i;
						for(char c; i<str.length() && (c = str.charAt(i))!='='; i++)
							if(c == '\\') {
								i++;
								continue;
							}
						int i1 = i;
						if(i<str.length() && str.charAt(i) == '=') {
							for(char c; i<str.length() && (c = str.charAt(i))!='?'; i++)
								if(c == '\\') {
									i++;
									continue;
								}
							String key = str.substring(i0+1, i1).replaceAll("(\\\\)(.)", "$2");
							String value = str.substring(i1+1, i>str.length()?str.length():i).replaceAll("(\\\\)(.)", "$2");
							if(key.equals(""))
								def = value;
							else if(locationValue.equals(key) || key.equals(locationValue.toString())) {
								found = true;
								replacement = value;
							}
						}
					}
					
					if(!found)
						replacement = def;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			b.append(replacement);
		}
		b.append(s, lastEnd, s.length());
		return b.toString();
	}
	
	/* ### STREAMS ### */
	
	public static void transferFully(InputStream in, OutputStream out) throws IOException {
		byte[] buff = new byte[1024];
		int l;
		while((l=in.read(buff, 0, buff.length)) != -1)
			out.write(buff, 0, l);
	}
	
	public static byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transferFully(in, out);
		return out.toByteArray();
	}
	
	public static void writeToFile(File f, byte[] data) throws IOException {
		FileOutputStream os = new FileOutputStream(f, false);
		try {
			os.write(data);
		} finally {
			try {
				os.close();
			} catch (Exception e) {}
		}
	}
	
	/* ### TASKS ### */

	public static void asyncOperation(Runnable runnable) {
		ASYNCING.registerAction(runnable);
	}
	
	public static void syncOperation(Runnable runnable) {
		SYNCING.registerAction(getPlugin(), runnable);
	}
	
	public static void runTask(Task task) {
		new RunTask(task, getPlugin());
	}
	
	public static void interruptTasks() {
		RunTask.interruptAll();
	}
	
	private static final AsyncUtil ASYNCING = new AsyncUtil();
	
	private static final class AsyncUtil implements Runnable{
		
		private ArrayDeque<Runnable> runnables = new ArrayDeque<Runnable>();
		private boolean active = false;
		
		private synchronized void registerAction(Runnable r) {
			runnables.addLast(r);
			ensureRunning();
			notifyAll();
		}
		
		private synchronized void ensureRunning() {
			if(!active)
				new Thread(this).start();
		}
		
		private synchronized Runnable nextRun() {
			Runnable r = runnables.pollFirst();
			if(r != null && runnables.size() == 0) {
				runnables.clear();
			}
			return r;
		}
		
		public void run() {
			synchronized (this) {
				if(active)
					return;
				active = true;
			}
			
			try {
				while(true) {
					Runnable r = nextRun();
					synchronized (this) {
						if(r == null && (r=nextRun()) == null) {
							Plugin p = null;
							try {
								p = JavaPlugin.getProvidingPlugin(BaseUtil.class);
							} catch(Exception e) {}
							if(p == null || !p.isEnabled()) {
								active = false;
								break;
							} else {
								try {
									wait();
								} catch (InterruptedException e) {}
							}
						} else {
							try {
								r.run();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}	
					}
				}
			} catch(RuntimeException e) {
				synchronized (this) {
					active = false;
				}
				throw e;
			}
		}
	}
	
	private static final SyncUtil SYNCING = new SyncUtil();
	
	private static final class SyncUtil implements Runnable {
		
		private ArrayDeque<Runnable> runnables = new ArrayDeque<Runnable>();		
		private int id = -1;
		
		private synchronized void registerAction(Plugin p, Runnable r) {
			runnables.addLast(r);
			ensureRunning(p);
		}
		
		private synchronized void ensureRunning(Plugin p) {
			if(!Bukkit.getScheduler().isQueued(id)) {
				id = Bukkit.getScheduler().runTaskTimer(p, this, 0, 0).getTaskId();
			}
		}
		
		private synchronized Runnable nextRun() {
			Runnable r = runnables.pollFirst();
			if(r != null && runnables.size() == 0) {
				runnables.clear();
			}
			return r;
		}
		
		public void run() {
			if(!Bukkit.isPrimaryThread())
				throw new IllegalStateException("Not primary thread");
			
			Runnable r;
			while((r=nextRun()) != null)
				try {
					r.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
	}
	
	static final class RunTask implements Runnable {
		
		private static final Collection<RunTask> tasks = new ArrayDeque<RunTask>();
		
		private static void interruptAll() {
			synchronized (RunTask.class) {
				for(RunTask t : tasks)
					t.cancel();
			}
		}
		
		private boolean running = false;
		private boolean canceld = false;
		private final Plugin plugin;
		private Thread thread = null;
		private Task current = null;
		private ArrayDeque<Listener> listeners = new ArrayDeque<Listener>();
		
		private RunTask(Task initialTask, Plugin plugin) {
			this.plugin = plugin;
			current = initialTask;
			new Thread(this).start();
		}

		public void run() {
			try {
				synchronized (this) {
					if (thread != null)
						throw new IllegalStateException("Can't start same task twice");
					thread = Thread.currentThread();
					if (canceld)
						return;
					running = true;
					synchronized (RunTask.class) {
						tasks.add(this);
					}
				}

				while (current != null && !canceld) {
					addListener(current);
					current.start(this);
					for(Listener l : listeners)
						HandlerList.unregisterAll(l);
					listeners.clear();
					current = current.getNext();
				}
			} finally {
				synchronized (this) {
					current = null;
					running = false;
					canceld = true;
					synchronized (RunTask.class) {
						tasks.remove(this);
					}
				}
			}
		}
		
		public Plugin getPlugin() {
			return plugin;
		}
		
		public void addListener(Listener l) {
			plugin.getServer().getPluginManager().registerEvents(current, plugin);
		}
		
		public void cancel() {
			canceld = true;
			if(isActive() && current != null) {
				current.interrupt();
			}
		}
		
		public boolean isActive() {
			synchronized (this) {
				return running && thread.isAlive();
			}
		}
		
	}
	
	/* ### CLASSES ### */
	
	private static String pack = null;
	
	public static Field getBukkitField(String cls, String name) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		Field f = getBukkitClass(cls).getDeclaredField(name);
		f.setAccessible(true);
		return f;
	}
	
	public static Field getServerField(String cls, String name) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		Field f = getServerClass(cls).getDeclaredField(name);
		f.setAccessible(true);
		return f;
	}
	
	public static Method getBukkitMethod(String cls, String name, Class<?> ... classes) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Method m = getBukkitClass(cls).getDeclaredMethod(name, classes);
		m.setAccessible(true);
		return m;
	}
	
	public static Method getServerMethod(String cls, String name, Class<?> ... classes) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Method m = getServerClass(cls).getDeclaredMethod(name, classes);
		m.setAccessible(true);
		return m;
	}
	
	public static Class<?> getBukkitClass(String cls) throws ClassNotFoundException {
		return Class.forName("org.bukkit.craftbukkit." + getPackage() + "." + cls);
	}
	
	public static Class<?> getServerClass(String cls) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + getPackage() + "." + cls);
	}
	
	private static String getPackage() {
		if(pack == null) {
			String server = Bukkit.getServer().getClass().getName();
			int i = server.indexOf('.', server.indexOf('.', server.indexOf('.')+1)+1)+1;
			pack = server.substring(i, server.indexOf('.', i));
		}
		return pack;
	}
	
	/* ### GET PLUGIN ### */
	
	private static Plugin getPlugin() {
		return JavaPlugin.getProvidingPlugin(BaseUtil.class);
	}
	
}
