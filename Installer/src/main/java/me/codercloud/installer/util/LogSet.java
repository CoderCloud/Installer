package me.codercloud.installer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class LogSet {

	private final HashSet<LogListener> listeners = new HashSet<LogListener>();
	private final HashMap<String, Log> logMap = new HashMap<String, Log>();
	private final ArrayList<Log> logs = new ArrayList<Log>();
	private final List<Log> logList = Collections.unmodifiableList(logs);
	private int level = 0;
	
	public synchronized Log getLog(String name) {
		Log l = logMap.get(name.toLowerCase());
		if (l == null) {
			l = new Log(name);
			logMap.put(l.name.toLowerCase(), l);
			logs.add(l);
		}
		return l;
	}

	public List<Log> getLogs() {
		return logList;
	}

	public synchronized void log(String name, int level, String msg) {
		getLog(name).log(level, msg);
	}

	public int getLevel() {
		return level;
	}
	
	public String[] toMessage(String nameSeperator, String messageSeperator, ChatColor[] names, ChatColor[] messages) {
		String[] msg = new String[logs.size()];
		
		for (int i = 0; i < msg.length; i++) {
			msg[i] = logs.get(i).toMessage(nameSeperator, messageSeperator, names, messages);
		}
		return msg;
	}
	
	public void addListener(LogListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(LogListener listener) {
		this.listeners.remove(listener);
	}
	
	private void onLog() {
		for(LogListener l : listeners)
			l.notifyChange(this);
	}
	
	public final class Log {

		private final ArrayList<LogEntry> entries;
		private final List<LogEntry> entryList;
		private final String name;
		private int level = 0;

		private Log(String name) {
			this.entries = new ArrayList<LogEntry>();
			this.entryList = Collections.unmodifiableList(entries);
			this.name = name;
		}
		
		public List<LogEntry> getEntries() {
			return entryList;
		}
		
		public String getName() {
			return name;
		}

		public synchronized void log(int level, String msg) {
			level = level<0?0:level;
			if (level > this.level) {
				this.level = level;
				if (LogSet.this.level < level)
					LogSet.this.level = level;
			}
			entries.add(new LogEntry(level, msg));
			onLog();
		}

		public int getLevel() {
			return level;
		}
		
		public ItemStack toItemStack(ChatColor[] names, Material[] materials, Short[] duriablities, ChatColor[] messages) {
			int level = this.level;
			int length = this.entries.size();
			
			ChatColor name = BaseUtil.getClipped(names, level, names.length>0?names[names.length-1]:ChatColor.RESET);
			Material material = BaseUtil.getClipped(materials, level, materials.length>0?materials[materials.length-1]:Material.PAPER);
			short duriablity = BaseUtil.getClipped(duriablities, level, duriablities.length>0?duriablities[duriablities.length-1]:Short.valueOf((short) 0));
			String[] lore = new String[length];
			
			for(int i = 0; i<length; i++) {
				LogEntry e = entries.get(i);
				ChatColor color = BaseUtil.getClipped(messages, e.getLevel(), messages.length>0?messages[messages.length-1]:ChatColor.RESET);
				lore[i] = color + e.getMessage();
			}
			
			return BaseUtil.setNameAndLore(new ItemStack(material, 1, duriablity), name + this.name, lore);
		}
		
		public String toMessage(String nameSeperator, String messageSeperator, ChatColor[] names, ChatColor[] messages) {
			int level = this.level;
			int length = this.entries.size();
			StringBuilder b = new StringBuilder();
			ChatColor name = BaseUtil.getClipped(names, level, names.length>0?names[names.length-1]:ChatColor.RESET);
			b.append(name).append(this.name).append(nameSeperator);
			
			for(int i = 0; i<length; i++) {
				LogEntry e = entries.get(i);
				ChatColor color = BaseUtil.getClipped(messages, e.getLevel(), messages.length>0?messages[messages.length-1]:ChatColor.RESET);
				if(i != 0)
					b.append(messageSeperator);
				b.append(color).append(e.getMessage());
			}
			
			return b.toString();
		}
		
		public final class LogEntry {

			private final int level;
			private final String msg;

			private LogEntry(int level, String msg) {
				this.level = level;
				this.msg = msg;
			}

			public int getLevel() {
				return level;
			}

			public String getMessage() {
				return msg;
			}

		}

	}
	
	public static interface LogListener {
		public void notifyChange(LogSet set);
	}
}
