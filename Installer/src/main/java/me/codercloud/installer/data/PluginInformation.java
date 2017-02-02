package me.codercloud.installer.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.Data.DataMap;

public class PluginInformation {
	
	private String stage;
	private String name;
	private String id;
	private String slug;
	private List<VersionInformation> files = new ArrayList<VersionInformation>();
	
	public PluginInformation(DataMap data) {
		name=data.getAsString("name");
		id=data.getAsString("id");
		slug=data.getAsString("slug");
		stage=data.getAsString("stage");
		if(id==null)
			throw new IllegalArgumentException();
	}
	
	public void readVersionData(DataMap d) {
		files.add(new VersionInformation(files.size(), d));
	}
	
	public String getSlug() {
		return slug;
	}
	
	public String getName() {
		return name;
	}
	
	public String getStage() {
		return stage;
	}
	
	public String getId() {
		return id;
	}
	
	public List<VersionInformation> getFiles() {
		return files;
	}
	
	@Override
	public String toString() {
		return name+"["+id+" - "+files.size()+"]";
	}
	
	public static class VersionInformation {
		
		private int number;
		private String fileName;
		private String releaseType;
		private String download;
		private String name;
		
		public VersionInformation(int number, DataMap m) {
			this.number = number;
			fileName = m.getAsString("fileName");
			releaseType = m.getAsString("releaseType");
			download = m.getAsString("downloadUrl");
			name = m.getAsString("name");
		}
		
		public int getNumber() {
			return number;
		}
		
		public String getName() {
			return name;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public String getDownload() {
			return download;
		}
		
		public String getReleaseType() {
			return releaseType;
		}

		public ItemStack asItemStack(boolean selected) {
			ItemStack i = null;
			
			ArrayList<String> lore = new ArrayList<String>();
			
			if(selected) {
				i = new ItemStack(Material.NETHER_STAR);
				BaseUtil.setName(i, ChatColor.GREEN + name);
				lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + "SELECTED");
			} else {
				if(releaseType.equals("release"))
					i = BaseUtil.setName(new ItemStack(Material.GOLD_INGOT), ChatColor.YELLOW + name);
				else if(releaseType.equals("beta"))
					i = BaseUtil.setName(new ItemStack(Material.IRON_INGOT), ChatColor.AQUA + name);
				else
					i = BaseUtil.setName(new ItemStack(Material.NETHER_BRICK_ITEM), ChatColor.GRAY + name);
				lore.add("");
			}
			
			lore.add("Type: " + getReleaseType());
			
			BaseUtil.setLore(i, lore.toArray(new String[lore.size()]));
			
			return i;
		}
		
	}

	public ItemStack asItemStack(boolean selected) {
		ItemStack i = null;
		String[] lore = new String[2];
		
		if(selected) {
			i = new ItemStack(Material.LAVA_BUCKET);
			BaseUtil.setName(i, ChatColor.YELLOW + getName());
			lore[0] = ChatColor.GREEN + "" + ChatColor.ITALIC + "SELECTED";
		} else {
			i = new ItemStack(Material.BUCKET);
			BaseUtil.setName(i, ChatColor.WHITE + getName());
			lore[0] = "";
		}
		
		lore[1] = "Stage: " + getStage();
		BaseUtil.setLore(i, lore);
		
		return i;
	}
	
}
