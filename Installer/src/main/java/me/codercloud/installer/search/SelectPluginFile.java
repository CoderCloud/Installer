package me.codercloud.installer.search;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.codercloud.installer.data.PluginData;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.menu.SelectManyMenuPoint;

public class SelectPluginFile extends SelectManyMenuPoint<PluginData> {

	
	public SelectPluginFile(Collection<PluginData> files) {
		super(2, ChatColor.GREEN + "Select Files:");
		setObjects(files);
		setSelectAll(true);
	}
	
	@Override
	public ItemStack asItem(PluginData object, int slot, boolean selected) {
		if(object != null)
			return object.asItemStack(selected);
		return null;
	}
	
	public ItemStack getSubmitButton() {
		Collection<PluginData> selected = getSelectedObjects();
		
		int up = 0;
		int in = 0;
		for(PluginData d : selected)
				if(d.isUpdate())
					up++;
				else
					in++;
						
		return BaseUtil.setLore(BaseUtil.setName(new ItemStack(Material.EMERALD_BLOCK), ChatColor.GREEN + "Install:"),
				in + " plugins will be installed",
				up + " plugins will be updated"
				);
	}

	@Override
	public boolean onLeftClickSubmit(boolean shift) {
		setNext(new InstallPluginFiles(getSelectedObjects().toArray(new PluginData[getSelectedObjects().size()])));
		return false;
	}

	@Override
	public void onClose() {
		getPlayer().sendMessage(ChatColor.RED + "Your installation got canceled");
	}

}
